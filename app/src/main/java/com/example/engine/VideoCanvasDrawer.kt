package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.LinearGradient
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Brush
import com.example.model.SceneItem
import com.example.model.VideoProject
import kotlin.math.max
import kotlin.math.min

object VideoCanvasDrawer {

    data class ActiveSceneState(
        val scene: SceneItem,
        val sceneIndex: Int,
        val sceneProgress: Float,
        val totalScenes: Int
    )

    fun calculateActiveScene(scenes: List<SceneItem>, currentTime: Float): ActiveSceneState {
        if (scenes.isEmpty()) {
            return ActiveSceneState(
                scene = SceneItem(1, "Sample", "Sample"),
                sceneIndex = 0,
                sceneProgress = 0f,
                totalScenes = 1
            )
        }

        var accumulatedTime = 0.0f
        for (i in scenes.indices) {
            val s = scenes[i]
            val duration = max(0.5f, s.duration)
            if (currentTime >= accumulatedTime && currentTime <= accumulatedTime + duration) {
                val progress = ((currentTime - accumulatedTime) / duration).coerceIn(0f, 1f)
                return ActiveSceneState(s, i, progress, scenes.size)
            }
            accumulatedTime += duration
        }

        // If at the end
        val lastScene = scenes.last()
        return ActiveSceneState(lastScene, scenes.size - 1, 1.0f, scenes.size)
    }

    /**
     * Draw on Jetpack Compose DrawScope for real-time smooth 60fps preview in UI
     */
    fun drawOnComposeScope(
        drawScope: DrawScope,
        scenes: List<SceneItem>,
        currentTime: Float,
        theme: String,
        bitmapCache: Map<String, Bitmap> = emptyMap()
    ) {
        val w = drawScope.size.width
        val h = drawScope.size.height
        if (w <= 0f || h <= 0f) return

        val state = calculateActiveScene(scenes, currentTime)
        val scene = state.scene
        val progress = state.sceneProgress

        // 1. Draw Background
        val imageBitmap = scene.imageUri?.let { bitmapCache[it] }
        if (imageBitmap != null && !imageBitmap.isRecycled) {
            drawScope.drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val scale = 1.0f + (progress * 0.08f)
                val targetW = w * scale
                val targetH = h * scale
                val dx = (w - targetW) / 2f
                val dy = (h - targetH) / 2f

                val srcRect = android.graphics.Rect(0, 0, imageBitmap.width, imageBitmap.height)
                val dstRect = RectF(dx, dy, dx + targetW, dy + targetH)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                native.drawBitmap(imageBitmap, srcRect, dstRect, paint)

                // Dark vignette overlay
                paint.color = AndroidColor.argb(170, 11, 15, 25)
                native.drawRect(0f, 0f, w, h, paint)
            }
        } else {
            // Gradient Solid
            val startColor = try {
                Color(android.graphics.Color.parseColor(scene.bgColor))
            } catch (_: Exception) {
                Color(0xFF1E1B4B)
            }
            val endColor = Color(0xFF0B0F19)

            drawScope.drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor, endColor),
                    startY = 0f,
                    endY = h
                ),
                size = Size(w, h)
            )

            // Ambient background lighting circle
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = w * 0.45f,
                center = Offset(w * 0.8f, h * 0.22f)
            )
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = w * 0.35f,
                center = Offset(w * 0.2f, h * 0.75f)
            )
        }

        // 2. Entrance & Exit animation
        var opacity = 1.0f
        var offsetY = 0.0f
        if (progress < 0.15f) {
            opacity = (progress / 0.15f).coerceIn(0f, 1f)
            offsetY = (1.0f - opacity) * 35.0f
        } else if (progress > 0.85f) {
            opacity = ((1.0f - progress) / 0.15f).coerceIn(0f, 1f)
            offsetY = ((progress - 0.85f) / 0.15f) * -25.0f
        }

        val baseScale = w / 720.0f // normalized scale against 720p

        // 3. Draw Scene Title & Narration
        drawScope.drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            native.save()

            val alphaInt = (opacity * 255).toInt().coerceIn(0, 255)

            when (theme) {
                VideoProject.THEME_BOLD_IMPACT -> {
                    // Bold impact: Yellow title, heavy punchy text
                    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = AndroidColor.argb(alphaInt, 234, 179, 8)
                        textSize = 50.0f * baseScale
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    native.drawText(scene.title.uppercase(), w / 2f, h * 0.30f + offsetY, titlePaint)

                    val narrationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = AndroidColor.argb(alphaInt, 255, 255, 255)
                        textSize = 42.0f * baseScale
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(8f * baseScale, 0f, 4f * baseScale, AndroidColor.argb((alphaInt * 0.8).toInt(), 0, 0, 0))
                    }
                    drawWrappedText(native, scene.narration, w / 2f, h * 0.44f + offsetY, w - (100f * baseScale), 58.0f * baseScale, narrationPaint)
                }

                VideoProject.THEME_MINIMAL_CLEAN -> {
                    // Minimal clean: Slate silver title, refined typography
                    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = AndroidColor.argb(alphaInt, 203, 213, 225)
                        textSize = 32.0f * baseScale
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        textAlign = Paint.Align.CENTER
                    }
                    native.drawText(scene.title, w / 2f, h * 0.32f + offsetY, titlePaint)

                    val narrationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = AndroidColor.argb(alphaInt, 255, 255, 255)
                        textSize = 38.0f * baseScale
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(6f * baseScale, 0f, 3f * baseScale, AndroidColor.argb((alphaInt * 0.6).toInt(), 0, 0, 0))
                    }
                    drawWrappedText(native, scene.narration, w / 2f, h * 0.44f + offsetY, w - (120f * baseScale), 52.0f * baseScale, narrationPaint)
                }

                else -> {
                    // Modern Pop: Glowing rounded pill badge
                    val titleText = scene.title.uppercase()
                    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = AndroidColor.argb(alphaInt, 15, 23, 42)
                        textSize = 34.0f * baseScale
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }

                    val textWidth = titlePaint.measureText(titleText)
                    val badgeY = h * 0.32f + offsetY
                    val badgeHeight = 52.0f * baseScale
                    val badgeWidth = textWidth + (48.0f * baseScale)
                    val badgeLeft = (w - badgeWidth) / 2.0f
                    val badgeTop = badgeY - (badgeHeight * 0.65f)

                    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = AndroidColor.argb(alphaInt, 245, 158, 11) // Amber #F59E0B
                    }
                    val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeWidth, badgeTop + badgeHeight)
                    native.drawRoundRect(badgeRect, 14.0f * baseScale, 14.0f * baseScale, badgePaint)

                    native.drawText(titleText, w / 2f, badgeY + (6.0f * baseScale), titlePaint)

                    val narrationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = AndroidColor.argb(alphaInt, 255, 255, 255)
                        textSize = 40.0f * baseScale
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(10f * baseScale, 0f, 4f * baseScale, AndroidColor.argb((alphaInt * 0.85).toInt(), 0, 0, 0))
                    }
                    drawWrappedText(native, scene.narration, w / 2f, h * 0.46f + offsetY, w - (110f * baseScale), 56.0f * baseScale, narrationPaint)
                }
            }

            native.restore()
        }

        // 4. Bottom Scene & Overall Progress Bar
        val barMarginX = 36.0f * baseScale
        val barY = h - (45.0f * baseScale)
        val barWidth = w - (barMarginX * 2.0f)
        val barHeight = 6.0f * baseScale

        // Background track
        drawScope.drawRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(barMarginX, barY),
            size = Size(barWidth, barHeight)
        )
        // Progress fill
        drawScope.drawRect(
            color = Color(0xFF6366F1),
            topLeft = Offset(barMarginX, barY),
            size = Size(barWidth * progress, barHeight)
        )

        // Scene dot pills
        val dotRadius = 4.0f * baseScale
        val dotSpacing = 16.0f * baseScale
        val totalDotsWidth = (state.totalScenes * (dotRadius * 2 + dotSpacing)) - dotSpacing
        val startDotX = (w - totalDotsWidth) / 2.0f
        val dotY = barY - (18.0f * baseScale)

        for (i in 0 until state.totalScenes) {
            val cx = startDotX + i * (dotRadius * 2 + dotSpacing) + dotRadius
            val dotColor = if (i == state.sceneIndex) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.4f)
            drawScope.drawCircle(
                color = dotColor,
                radius = if (i == state.sceneIndex) dotRadius * 1.3f else dotRadius,
                center = Offset(cx, dotY)
            )
        }
    }

    /**
     * Draw to a software/hardware Android Canvas for exporting video frames
     */
    fun drawOnAndroidCanvas(
        canvas: android.graphics.Canvas,
        width: Int,
        height: Int,
        scenes: List<SceneItem>,
        currentTime: Float,
        theme: String,
        bitmapCache: Map<String, Bitmap> = emptyMap()
    ) {
        val w = width.toFloat()
        val h = height.toFloat()
        val state = calculateActiveScene(scenes, currentTime)
        val scene = state.scene
        val progress = state.sceneProgress

        val imageBitmap = scene.imageUri?.let { bitmapCache[it] }
        if (imageBitmap != null && !imageBitmap.isRecycled) {
            val scale = 1.0f + (progress * 0.08f)
            val targetW = w * scale
            val targetH = h * scale
            val dx = (w - targetW) / 2f
            val dy = (h - targetH) / 2f

            val srcRect = android.graphics.Rect(0, 0, imageBitmap.width, imageBitmap.height)
            val dstRect = RectF(dx, dy, dx + targetW, dy + targetH)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(imageBitmap, srcRect, dstRect, paint)

            paint.color = AndroidColor.argb(170, 11, 15, 25)
            canvas.drawRect(0f, 0f, w, h, paint)
        } else {
            val parsedColor = try {
                android.graphics.Color.parseColor(scene.bgColor)
            } catch (_: Exception) {
                android.graphics.Color.rgb(30, 27, 75)
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(0f, 0f, 0f, h, parsedColor, AndroidColor.rgb(11, 15, 25), Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, w, h, paint)

            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.argb(10, 255, 255, 255)
            }
            canvas.drawCircle(w * 0.8f, h * 0.22f, w * 0.45f, circlePaint)
            canvas.drawCircle(w * 0.2f, h * 0.75f, w * 0.35f, circlePaint)
        }

        var opacity = 1.0f
        var offsetY = 0.0f
        if (progress < 0.15f) {
            opacity = (progress / 0.15f).coerceIn(0f, 1f)
            offsetY = (1.0f - opacity) * 35.0f
        } else if (progress > 0.85f) {
            opacity = ((1.0f - progress) / 0.15f).coerceIn(0f, 1f)
            offsetY = ((progress - 0.85f) / 0.15f) * -25.0f
        }

        val baseScale = w / 720.0f
        val alphaInt = (opacity * 255).toInt().coerceIn(0, 255)

        when (theme) {
            VideoProject.THEME_BOLD_IMPACT -> {
                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(alphaInt, 234, 179, 8)
                    textSize = 50.0f * baseScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(scene.title.uppercase(), w / 2f, h * 0.30f + offsetY, titlePaint)

                val narrationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(alphaInt, 255, 255, 255)
                    textSize = 42.0f * baseScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    setShadowLayer(8f * baseScale, 0f, 4f * baseScale, AndroidColor.argb((alphaInt * 0.8).toInt(), 0, 0, 0))
                }
                drawWrappedText(canvas, scene.narration, w / 2f, h * 0.44f + offsetY, w - (100f * baseScale), 58.0f * baseScale, narrationPaint)
            }

            VideoProject.THEME_MINIMAL_CLEAN -> {
                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(alphaInt, 203, 213, 225)
                    textSize = 32.0f * baseScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(scene.title, w / 2f, h * 0.32f + offsetY, titlePaint)

                val narrationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(alphaInt, 255, 255, 255)
                    textSize = 38.0f * baseScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                    setShadowLayer(6f * baseScale, 0f, 3f * baseScale, AndroidColor.argb((alphaInt * 0.6).toInt(), 0, 0, 0))
                }
                drawWrappedText(canvas, scene.narration, w / 2f, h * 0.44f + offsetY, w - (120f * baseScale), 52.0f * baseScale, narrationPaint)
            }

            else -> {
                val titleText = scene.title.uppercase()
                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(alphaInt, 15, 23, 42)
                    textSize = 34.0f * baseScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }

                val textWidth = titlePaint.measureText(titleText)
                val badgeY = h * 0.32f + offsetY
                val badgeHeight = 52.0f * baseScale
                val badgeWidth = textWidth + (48.0f * baseScale)
                val badgeLeft = (w - badgeWidth) / 2.0f
                val badgeTop = badgeY - (badgeHeight * 0.65f)

                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(alphaInt, 245, 158, 11)
                }
                val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeWidth, badgeTop + badgeHeight)
                canvas.drawRoundRect(badgeRect, 14.0f * baseScale, 14.0f * baseScale, badgePaint)

                canvas.drawText(titleText, w / 2f, badgeY + (6.0f * baseScale), titlePaint)

                val narrationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(alphaInt, 255, 255, 255)
                    textSize = 40.0f * baseScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    setShadowLayer(10f * baseScale, 0f, 4f * baseScale, AndroidColor.argb((alphaInt * 0.85).toInt(), 0, 0, 0))
                }
                drawWrappedText(canvas, scene.narration, w / 2f, h * 0.46f + offsetY, w - (110f * baseScale), 56.0f * baseScale, narrationPaint)
            }
        }

        // Progress bar at bottom
        val barMarginX = 36.0f * baseScale
        val barY = h - (45.0f * baseScale)
        val barWidth = w - (barMarginX * 2.0f)
        val barHeight = 6.0f * baseScale

        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(64, 255, 255, 255)
        }
        canvas.drawRect(barMarginX, barY, barMarginX + barWidth, barY + barHeight, trackPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(99, 102, 241)
        }
        canvas.drawRect(barMarginX, barY, barMarginX + (barWidth * progress), barY + barHeight, fillPaint)
    }

    private fun drawWrappedText(
        canvas: android.graphics.Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ) {
        val words = text.split(" ")
        var line = ""
        var currentY = y

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val testWidth = paint.measureText(testLine)
            if (testWidth > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, x, currentY, paint)
                line = word
                currentY += lineHeight
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, currentY, paint)
        }
    }
}
