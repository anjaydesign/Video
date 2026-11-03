package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.model.SceneItem
import com.example.model.VideoProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class VideoEncoder(private val context: Context) {

    private val isCancelled = AtomicBoolean(false)

    fun cancel() {
        isCancelled.set(true)
    }

    suspend fun renderToMp4(
        project: VideoProject,
        bitmapCache: Map<String, Bitmap> = emptyMap(),
        fps: Int = 24,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.Default) {
        isCancelled.set(false)

        val (rawW, rawH) = when (project.resolution) {
            VideoProject.RES_540x960 -> 540 to 960
            VideoProject.RES_1080x1920 -> 1080 to 1920
            else -> 720 to 1280
        }

        // Encoders require dimensions to be divisible by 16
        val width = (rawW / 16) * 16
        val height = (rawH / 16) * 16

        val totalDuration = project.totalDuration.coerceAtLeast(1.0f)
        val totalFrames = (totalDuration * fps).toInt()
        val frameDurationUs = (1_000_000L / fps)

        val outputDir = File(context.cacheDir, "rendered_videos").apply { mkdirs() }
        val outputFile = File(outputDir, "openmontage_${project.id}_${System.currentTimeMillis()}.mp4")
        if (outputFile.exists()) outputFile.delete()

        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val bitrate = when {
            width >= 1080 -> 4_000_000
            width >= 720 -> 2_500_000
            else -> 1_200_000
        }

        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val codec = MediaCodec.createEncoderByType(mimeType)
        var muxer: MediaMuxer? = null
        var trackIndex = -1
        var muxerStarted = false

        val frameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val frameCanvas = Canvas(frameBitmap)
        val argbArray = IntArray(width * height)
        val yuvArray = ByteArray(width * height * 3 / 2)

        try {
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val bufferInfo = MediaCodec.BufferInfo()
            var currentFrame = 0

            while (currentFrame < totalFrames) {
                if (isCancelled.get()) {
                    throw Exception("Render dibatalkan oleh pengguna.")
                }

                val currentTime = (currentFrame.toFloat() / totalFrames) * totalDuration

                // Draw frame
                VideoCanvasDrawer.drawOnAndroidCanvas(
                    canvas = frameCanvas,
                    width = width,
                    height = height,
                    scenes = project.scenes,
                    currentTime = currentTime,
                    theme = project.theme,
                    bitmapCache = bitmapCache
                )

                frameBitmap.getPixels(argbArray, 0, width, 0, 0, width, height)
                encodeYUV420SP(yuvArray, argbArray, width, height)

                // Feed into MediaCodec
                val inputBufferIndex = codec.dequeueInputBuffer(10_000L)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(yuvArray)
                    val presentationTimeUs = currentFrame * frameDurationUs
                    val flags = if (currentFrame == totalFrames - 1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                    codec.queueInputBuffer(inputBufferIndex, 0, yuvArray.size, presentationTimeUs, flags)
                    currentFrame++
                }

                // Drain output buffers
                drainCodec(codec, muxer, bufferInfo, false) { idx, started ->
                    trackIndex = idx
                    muxerStarted = started
                }

                val progress = (currentFrame.toFloat() / totalFrames).coerceIn(0f, 1f)
                withContext(Dispatchers.Main) {
                    onProgress(progress)
                }
            }

            // Drain remaining
            drainCodec(codec, muxer, bufferInfo, true) { idx, started ->
                trackIndex = idx
                muxerStarted = started
            }

        } finally {
            try {
                codec.stop()
                codec.release()
            } catch (_: Exception) {}

            try {
                if (muxerStarted && muxer != null) {
                    muxer.stop()
                    muxer.release()
                }
            } catch (_: Exception) {}

            frameBitmap.recycle()
        }

        outputFile
    }

    private fun drainCodec(
        codec: MediaCodec,
        muxer: MediaMuxer?,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onMuxerStateChange: (trackIndex: Int, muxerStarted: Boolean) -> Unit
    ) {
        var trackIdx = -1
        var muxerStarted = false

        while (true) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000L)
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (muxer != null) {
                    val newFormat = codec.outputFormat
                    trackIdx = muxer.addTrack(newFormat)
                    muxer.start()
                    muxerStarted = true
                    onMuxerStateChange(trackIdx, true)
                }
            } else if (outputBufferIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                if (outputBuffer != null && muxer != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    if (bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(if (trackIdx >= 0) trackIdx else 0, outputBuffer, bufferInfo)
                    }
                }
                codec.releaseOutputBuffer(outputBufferIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize

        var r: Int
        var g: Int
        var b: Int
        var y: Int
        var u: Int
        var v: Int
        var index = 0

        for (j in 0 until height) {
            for (i in 0 until width) {
                val color = argb[index++]
                r = (color and 0xff0000) shr 16
                g = (color and 0xff00) shr 8
                b = color and 0xff

                y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                yuv420sp[yIndex++] = (y.coerceIn(0, 255)).toByte()
                if (j % 2 == 0 && (index % 2 == 0)) {
                    if (uvIndex < yuv420sp.size - 1) {
                        yuv420sp[uvIndex++] = (u.coerceIn(0, 255)).toByte()
                        yuv420sp[uvIndex++] = (v.coerceIn(0, 255)).toByte()
                    }
                }
            }
        }
    }
}
