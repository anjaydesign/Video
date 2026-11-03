package com.example.model

data class SceneItem(
    val id: Int,
    val title: String,
    val narration: String,
    val duration: Float = 3.0f,
    val bgColor: String = "#1E1B4B",
    val imageUri: String? = null
)

data class VideoProject(
    val id: String = "proj_${System.currentTimeMillis()}",
    val name: String = "Proyek Video Baru",
    val theme: String = THEME_MODERN_POP,
    val resolution: String = RES_720x1280,
    val caption: String = "",
    val hashtags: List<String> = emptyList(),
    val scenes: List<SceneItem> = defaultScenes(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalDuration: Float
        get() = scenes.sumOf { it.duration.toDouble() }.toFloat()

    companion object {
        const val THEME_MODERN_POP = "modern_pop"
        const val THEME_MINIMAL_CLEAN = "minimal_clean"
        const val THEME_BOLD_IMPACT = "bold_impact"

        const val RES_720x1280 = "720x1280"
        const val RES_540x960 = "540x960"
        const val RES_1080x1920 = "1080x1920"

        fun defaultScenes(): List<SceneItem> = listOf(
            SceneItem(
                id = 1,
                title = "HOOK MENARIK",
                narration = "Apakah kamu sering mengalami kendala ini setiap hari?",
                duration = 3.0f,
                bgColor = "#B91C1C"
            ),
            SceneItem(
                id = 2,
                title = "KENALKAN SOLUSI",
                narration = "Inilah cara tercepat dan efisien untuk menyelesaikannya.",
                duration = 3.0f,
                bgColor = "#4338CA"
            ),
            SceneItem(
                id = 3,
                title = "FITUR UTAMA",
                narration = "Dibuat dengan teknologi canggih tanpa ribet.",
                duration = 3.0f,
                bgColor = "#1D4ED8"
            ),
            SceneItem(
                id = 4,
                title = "HASIL NYATA",
                narration = "Kualitas terjamin dan langsung bisa dibuktikan hari ini.",
                duration = 3.0f,
                bgColor = "#047857"
            ),
            SceneItem(
                id = 5,
                title = "CALL TO ACTION",
                narration = "Hubungi kami sekarang atau cek bio untuk promo eksklusif!",
                duration = 3.0f,
                bgColor = "#6D28D9"
            )
        )
    }
}
