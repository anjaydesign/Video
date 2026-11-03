package com.example.engine

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.SceneItem
import com.example.model.VideoProject
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProjectExporter {

    fun formatTimeSRT(seconds: Float): String {
        val totalMillis = (seconds * 1000).toLong()
        val hrs = totalMillis / 3600000
        val mins = (totalMillis % 3600000) / 60000
        val secs = (totalMillis % 60000) / 1000
        val millis = totalMillis % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hrs, mins, secs, millis)
    }

    fun generateSRT(scenes: List<SceneItem>): String {
        val sb = StringBuilder()
        var currentTime = 0.0f
        scenes.forEachIndexed { index, scene ->
            val startTime = currentTime
            val endTime = currentTime + scene.duration
            sb.append("${index + 1}\n")
            sb.append("${formatTimeSRT(startTime)} --> ${formatTimeSRT(endTime)}\n")
            val titlePrefix = if (scene.title.isNotBlank()) "${scene.title} - " else ""
            sb.append("$titlePrefix${scene.narration}\n\n")
            currentTime = endTime
        }
        return sb.toString().trim()
    }

    fun generateTXT(project: VideoProject): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(project.updatedAt))

        val sb = StringBuilder()
        sb.append("JUDUL: ${project.name}\n")
        sb.append("TANGGAL: $dateStr\n")
        sb.append("TOTAL DURASI: ${project.totalDuration}s\n")
        sb.append("TEMA: ${project.theme}\n")
        sb.append("RESOLUSI: ${project.resolution}\n\n")

        sb.append("--- NASKAH PER SCENE ---\n\n")
        project.scenes.forEachIndexed { idx, s ->
            sb.append("Scene ${idx + 1} [${s.duration}s]\n")
            sb.append("Visual: ${s.title}\n")
            sb.append("Narasi: \"${s.narration}\"\n\n")
        }

        if (project.caption.isNotBlank()) {
            sb.append("--- CAPTION ---\n")
            sb.append("${project.caption}\n\n")
        }

        if (project.hashtags.isNotEmpty()) {
            sb.append("--- TAGS ---\n")
            sb.append(project.hashtags.joinToString(" "))
            sb.append("\n")
        }

        return sb.toString()
    }

    fun generateJSON(project: VideoProject): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("name", project.name)
        root.put("theme", project.theme)
        root.put("resolution", project.resolution)
        root.put("caption", project.caption)
        root.put("updatedAt", project.updatedAt)

        val tagsArray = JSONArray()
        project.hashtags.forEach { tagsArray.put(it) }
        root.put("hashtags", tagsArray)

        val scenesArray = JSONArray()
        project.scenes.forEach { scene ->
            val sObj = JSONObject()
            sObj.put("id", scene.id)
            sObj.put("title", scene.title)
            sObj.put("narration", scene.narration)
            sObj.put("duration", scene.duration.toDouble())
            sObj.put("bgColor", scene.bgColor)
            sObj.put("imageUri", scene.imageUri)
            scenesArray.put(sObj)
        }
        root.put("scenes", scenesArray)

        return root.toString(2)
    }

    fun parseJSON(jsonStr: String): VideoProject? {
        return try {
            val root = JSONObject(jsonStr)
            val id = root.optString("id", "proj_${System.currentTimeMillis()}")
            val name = root.optString("name", "Proyek Impor")
            val theme = root.optString("theme", VideoProject.THEME_MODERN_POP)
            val resolution = root.optString("resolution", VideoProject.RES_720x1280)
            val caption = root.optString("caption", "")
            val updatedAt = root.optLong("updatedAt", System.currentTimeMillis())

            val tagsList = mutableListOf<String>()
            val tagsArray = root.optJSONArray("hashtags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(i))
                }
            }

            val scenesList = mutableListOf<SceneItem>()
            val scenesArray = root.optJSONArray("scenes")
            if (scenesArray != null) {
                for (i in 0 until scenesArray.length()) {
                    val sObj = scenesArray.getJSONObject(i)
                    scenesList.add(
                        SceneItem(
                            id = sObj.optInt("id", i + 1),
                            title = sObj.optString("title", "Scene ${i + 1}"),
                            narration = sObj.optString("narration", ""),
                            duration = sObj.optDouble("duration", 3.0).toFloat(),
                            bgColor = sObj.optString("bgColor", "#1E1B4B"),
                            imageUri = if (sObj.has("imageUri") && !sObj.isNull("imageUri")) sObj.optString("imageUri") else null
                        )
                    )
                }
            }

            VideoProject(
                id = id,
                name = name,
                theme = theme,
                resolution = resolution,
                caption = caption,
                hashtags = tagsList,
                scenes = if (scenesList.isNotEmpty()) scenesList else VideoProject.defaultScenes(),
                updatedAt = updatedAt
            )
        } catch (_: Exception) {
            null
        }
    }

    fun shareTextFile(context: Context, fileName: String, content: String, mimeType: String = "text/plain") {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)
            file.writeText(content)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan $fileName"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
