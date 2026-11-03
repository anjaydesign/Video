package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProjectRepository
import com.example.engine.MyRepublicParams
import com.example.engine.ProjectExporter
import com.example.engine.ScriptAssistant
import com.example.engine.ScriptResult
import com.example.engine.VideoEncoder
import com.example.model.SceneItem
import com.example.model.VideoProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class AppTab(val title: String, val icon: String) {
    DASHBOARD("Proyek", "📊"),
    SCRIPT("Naskah", "💡"),
    STORYBOARD("Storyboard", "📋"),
    STUDIO("Studio", "🎥"),
    EXPORT("Ekspor", "📤")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    private val videoEncoder = VideoEncoder(application)

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _currentProject = MutableStateFlow(VideoProject())
    val currentProject: StateFlow<VideoProject> = _currentProject.asStateFlow()

    private val _savedProjects = MutableStateFlow<List<VideoProject>>(emptyList())
    val savedProjects: StateFlow<List<VideoProject>> = _savedProjects.asStateFlow()

    // Script generator form
    val scriptCategory = MutableStateFlow(ScriptAssistant.CAT_PROMO_PRODUCT)
    val scriptTopic = MutableStateFlow("Sepatu Kulit Anti Air")
    val scriptAudience = MutableStateFlow("Pria Karir & Mahasiswa")
    val myRepSpeed = MutableStateFlow("100 Mbps")
    val myRepPrice = MutableStateFlow("Rp280.000/bln")
    val myRepBenefit = MutableStateFlow("Gratis Pemasangan, Full Fiber 1:1")

    private val _generatedScript = MutableStateFlow<ScriptResult?>(null)
    val generatedScript: StateFlow<ScriptResult?> = _generatedScript.asStateFlow()

    // Studio Playback & Render
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackTime = MutableStateFlow(0.0f)
    val playbackTime: StateFlow<Float> = _playbackTime.asStateFlow()

    private val _isRendering = MutableStateFlow(false)
    val isRendering: StateFlow<Boolean> = _isRendering.asStateFlow()

    private val _renderProgress = MutableStateFlow(0.0f)
    val renderProgress: StateFlow<Float> = _renderProgress.asStateFlow()

    private val _renderedVideoFile = MutableStateFlow<File?>(null)
    val renderedVideoFile: StateFlow<File?> = _renderedVideoFile.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Bitmap cache for canvas rendering
    private val _bitmapCache = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val bitmapCache: StateFlow<Map<String, Bitmap>> = _bitmapCache.asStateFlow()

    private var playbackJob: Job? = null

    init {
        val db = AppDatabase.getInstance(application)
        repository = ProjectRepository(db.projectDao())

        viewModelScope.launch {
            repository.allProjects.collect { list ->
                _savedProjects.value = list
            }
        }
    }

    fun switchTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab != AppTab.STUDIO) {
            pausePreview()
        }
    }

    fun createNewProject() {
        pausePreview()
        _currentProject.value = VideoProject(
            id = "proj_${System.currentTimeMillis()}",
            name = "Proyek Video Baru",
            theme = VideoProject.THEME_MODERN_POP,
            resolution = VideoProject.RES_720x1280,
            scenes = VideoProject.defaultScenes()
        )
        _renderedVideoFile.value = null
        _playbackTime.value = 0.0f
        showToast("Proyek baru dibuat!")
        switchTab(AppTab.STORYBOARD)
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            repository.saveProject(_currentProject.value)
            showToast("Proyek berhasil disimpan ke database!")
        }
    }

    fun loadProject(id: String) {
        viewModelScope.launch {
            val p = repository.getProject(id)
            if (p != null) {
                pausePreview()
                _currentProject.value = p
                _playbackTime.value = 0.0f
                _renderedVideoFile.value = null
                preloadBitmaps(p.scenes)
                showToast("Proyek '${p.name}' dimuat!")
                switchTab(AppTab.STORYBOARD)
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            showToast("Proyek dihapus!")
        }
    }

    // Script generator
    fun generateScript() {
        val result = ScriptAssistant.generate(
            category = scriptCategory.value,
            topic = scriptTopic.value,
            audience = scriptAudience.value,
            myrep = MyRepublicParams(
                speed = myRepSpeed.value,
                price = myRepPrice.value,
                benefit = myRepBenefit.value
            )
        )
        _generatedScript.value = result
        showToast("Naskah cerdas berhasil dibuat!")
    }

    fun applyScriptToStoryboard() {
        val script = _generatedScript.value ?: return
        val current = _currentProject.value
        val updatedScenes = current.scenes.toMutableList()

        for (i in 0 until minOf(updatedScenes.size, script.scenes.size)) {
            val genScene = script.scenes[i]
            updatedScenes[i] = updatedScenes[i].copy(
                title = genScene.title,
                narration = genScene.narration,
                duration = genScene.duration,
                bgColor = genScene.bgColor
            )
        }

        _currentProject.value = current.copy(
            name = script.title,
            caption = script.caption,
            hashtags = script.hashtags,
            scenes = updatedScenes
        )
        showToast("Naskah diterapkan ke 5 Scene Storyboard!")
        switchTab(AppTab.STORYBOARD)
    }

    // Storyboard operations
    fun updateScene(index: Int, scene: SceneItem) {
        val current = _currentProject.value
        if (index in current.scenes.indices) {
            val updated = current.scenes.toMutableList()
            updated[index] = scene
            _currentProject.value = current.copy(scenes = updated)
        }
    }

    fun updateProjectDetails(name: String, theme: String, resolution: String) {
        _currentProject.value = _currentProject.value.copy(
            name = name,
            theme = theme,
            resolution = resolution
        )
    }

    fun saveSceneImage(index: Int, uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val dir = File(context.filesDir, "scene_images").apply { mkdirs() }
                val targetFile = File(dir, "img_${System.currentTimeMillis()}_$index.jpg")
                FileOutputStream(targetFile).use { out ->
                    inputStream?.copyTo(out)
                }

                val savedPath = targetFile.absolutePath
                val bitmap = BitmapFactory.decodeFile(savedPath)
                if (bitmap != null) {
                    val currentMap = _bitmapCache.value.toMutableMap()
                    currentMap[savedPath] = bitmap
                    _bitmapCache.value = currentMap
                }

                withContext(Dispatchers.Main) {
                    val current = _currentProject.value
                    if (index in current.scenes.indices) {
                        val updated = current.scenes.toMutableList()
                        updated[index] = updated[index].copy(imageUri = savedPath)
                        _currentProject.value = current.copy(scenes = updated)
                    }
                    showToast("Gambar terpasang untuk Scene ${index + 1}!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Gagal memuat gambar: ${e.message}")
                }
            }
        }
    }

    fun removeSceneImage(index: Int) {
        val current = _currentProject.value
        if (index in current.scenes.indices) {
            val updated = current.scenes.toMutableList()
            updated[index] = updated[index].copy(imageUri = null)
            _currentProject.value = current.copy(scenes = updated)
            showToast("Gambar dihapus, kembali ke gradien warna.")
        }
    }

    private fun preloadBitmaps(scenes: List<SceneItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCache = _bitmapCache.value.toMutableMap()
            scenes.forEach { s ->
                val path = s.imageUri
                if (!path.isNullOrBlank() && !newCache.containsKey(path)) {
                    val f = File(path)
                    if (f.exists()) {
                        val bmp = BitmapFactory.decodeFile(path)
                        if (bmp != null) {
                            newCache[path] = bmp
                        }
                    }
                }
            }
            _bitmapCache.value = newCache
        }
    }

    // Playback control
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePreview()
        } else {
            playPreview()
        }
    }

    fun playPreview() {
        val total = _currentProject.value.totalDuration
        if (total <= 0f) return

        if (_playbackTime.value >= total) {
            _playbackTime.value = 0.0f
        }

        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val stepSec = 0.033f // ~30 fps update
            val stepMs = 33L
            while (_isPlaying.value) {
                delay(stepMs)
                val nextTime = _playbackTime.value + stepSec
                if (nextTime >= _currentProject.value.totalDuration) {
                    _playbackTime.value = _currentProject.value.totalDuration
                    _isPlaying.value = false
                    break
                } else {
                    _playbackTime.value = nextTime
                }
            }
        }
    }

    fun pausePreview() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun stopPreview() {
        pausePreview()
        _playbackTime.value = 0.0f
    }

    fun seekTo(time: Float) {
        _playbackTime.value = time.coerceIn(0f, _currentProject.value.totalDuration)
    }

    // Video Rendering
    fun startRender() {
        if (_isRendering.value) return
        pausePreview()
        _isRendering.value = true
        _renderProgress.value = 0.0f
        _renderedVideoFile.value = null

        viewModelScope.launch {
            try {
                val file = videoEncoder.renderToMp4(
                    project = _currentProject.value,
                    bitmapCache = _bitmapCache.value,
                    fps = 24
                ) { progress ->
                    _renderProgress.value = progress
                }
                _renderedVideoFile.value = file
                showToast("Video MP4 berhasil dirender!")
            } catch (e: Exception) {
                showToast("Gagal render: ${e.message}")
            } finally {
                _isRendering.value = false
            }
        }
    }

    fun cancelRender() {
        videoEncoder.cancel()
        _isRendering.value = false
        showToast("Render dibatalkan.")
    }

    // Export operations
    fun exportTXT(context: Context) {
        val project = _currentProject.value
        val txt = ProjectExporter.generateTXT(project)
        val fileName = "${project.name.replace("\\s+".toRegex(), "_")}_script.txt"
        ProjectExporter.shareTextFile(context, fileName, txt, "text/plain")
        showToast("Naskah .TXT siap dibagikan!")
    }

    fun exportSRT(context: Context) {
        val project = _currentProject.value
        val srt = ProjectExporter.generateSRT(project.scenes)
        val fileName = "${project.name.replace("\\s+".toRegex(), "_")}_subtitles.srt"
        ProjectExporter.shareTextFile(context, fileName, srt, "text/plain")
        showToast("Subtitle .SRT siap dibagikan!")
    }

    fun exportJSON(context: Context) {
        val project = _currentProject.value
        val json = ProjectExporter.generateJSON(project)
        val fileName = "${project.name.replace("\\s+".toRegex(), "_")}_backup.json"
        ProjectExporter.shareTextFile(context, fileName, json, "application/json")
        showToast("Data proyek .JSON diekspor!")
    }

    fun importJSON(jsonStr: String) {
        val imported = ProjectExporter.parseJSON(jsonStr)
        if (imported != null) {
            _currentProject.value = imported
            preloadBitmaps(imported.scenes)
            showToast("Proyek berhasil diimpor!")
            switchTab(AppTab.STORYBOARD)
        } else {
            showToast("Format JSON proyek tidak valid.")
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }
}
