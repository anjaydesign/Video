package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SceneItem
import com.example.model.VideoProject
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val theme: String,
    val resolution: String,
    val caption: String,
    val hashtagsCsv: String,
    val scenesJson: String,
    val updatedAt: Long
) {
    fun toVideoProject(): VideoProject {
        val tags = if (hashtagsCsv.isBlank()) emptyList() else hashtagsCsv.split(",").map { it.trim() }
        val sceneList = mutableListOf<SceneItem>()
        try {
            val jsonArray = JSONArray(scenesJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                sceneList.add(
                    SceneItem(
                        id = obj.optInt("id", i + 1),
                        title = obj.optString("title", "Scene ${i + 1}"),
                        narration = obj.optString("narration", ""),
                        duration = obj.optDouble("duration", 3.0).toFloat(),
                        bgColor = obj.optString("bgColor", "#1E1B4B"),
                        imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.optString("imageUri") else null
                    )
                )
            }
        } catch (_: Exception) {
            sceneList.addAll(VideoProject.defaultScenes())
        }

        return VideoProject(
            id = id,
            name = name,
            theme = theme,
            resolution = resolution,
            caption = caption,
            hashtags = tags,
            scenes = if (sceneList.isNotEmpty()) sceneList else VideoProject.defaultScenes(),
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromVideoProject(project: VideoProject): ProjectEntity {
            val jsonArray = JSONArray()
            for (scene in project.scenes) {
                val obj = JSONObject()
                obj.put("id", scene.id)
                obj.put("title", scene.title)
                obj.put("narration", scene.narration)
                obj.put("duration", scene.duration)
                obj.put("bgColor", scene.bgColor)
                obj.put("imageUri", scene.imageUri)
                jsonArray.put(obj)
            }

            return ProjectEntity(
                id = project.id,
                name = project.name,
                theme = project.theme,
                resolution = project.resolution,
                caption = project.caption,
                hashtagsCsv = project.hashtags.joinToString(","),
                scenesJson = jsonArray.toString(),
                updatedAt = project.updatedAt
            )
        }
    }
}
