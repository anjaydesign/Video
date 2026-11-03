package com.example.data

import com.example.model.VideoProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<VideoProject>> = projectDao.getAllProjects().map { list ->
        list.map { it.toVideoProject() }
    }

    suspend fun getProject(id: String): VideoProject? {
        return projectDao.getProjectById(id)?.toVideoProject()
    }

    suspend fun saveProject(project: VideoProject) {
        val updated = project.copy(updatedAt = System.currentTimeMillis())
        projectDao.insertProject(ProjectEntity.fromVideoProject(updated))
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }
}
