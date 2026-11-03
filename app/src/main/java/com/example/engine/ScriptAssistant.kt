package com.example.engine

import com.example.model.SceneItem

data class ScriptResult(
    val title: String,
    val hook: String,
    val cta: String,
    val caption: String,
    val hashtags: List<String>,
    val scenes: List<SceneItem>
)

data class MyRepublicParams(
    val speed: String = "",
    val price: String = "",
    val benefit: String = ""
)

object ScriptAssistant {

    const val CAT_PROMO_PRODUCT = "promo_product"
    const val CAT_PROMO_WIFI = "promo_wifi"
    const val CAT_EDUCATION = "education"
    const val CAT_STORYTELLING = "storytelling"
    const val CAT_SOCIAL_HOOK = "social_hook"
    const val CAT_SHORT_AD = "short_ad"

    fun generate(
        category: String,
        topic: String,
        audience: String,
        myrep: MyRepublicParams = MyRepublicParams()
    ): ScriptResult {
        val safeTopic = topic.ifBlank { "Layanan Unggulan" }
        val safeAudience = audience.ifBlank { "kamu" }

        return when (category) {
            CAT_PROMO_WIFI -> buildMyRepublicScript(safeTopic, safeAudience, myrep)
            CAT_PROMO_PRODUCT -> buildProductScript(safeTopic, safeAudience)
            CAT_EDUCATION -> buildEducationScript(safeTopic, safeAudience)
            CAT_STORYTELLING -> buildStoryScript(safeTopic, safeAudience)
            CAT_SOCIAL_HOOK -> buildSocialHookScript(safeTopic, safeAudience)
            else -> buildShortAdScript(safeTopic, safeAudience)
        }
    }

    private fun buildMyRepublicScript(topic: String, audience: String, myrep: MyRepublicParams): ScriptResult {
        val speed = if (myrep.speed.isNotBlank()) myrep.speed else "Kecepatan Hingga 250 Mbps"
        val price = if (myrep.price.isNotBlank()) myrep.price else "Harga Promo Spesial"
        val benefit = if (myrep.benefit.isNotBlank()) myrep.benefit else "Koneksi Stabil 100% Full Fiber Optic"

        return ScriptResult(
            title = "Internet Cepat: $topic",
            hook = "Internet rumah sering ngelag pas lagi push rank atau meeting?",
            cta = "Cek jangkauan area kamu sekarang juga!",
            caption = "Solusi internet anti putus-putus dengan $topic. Download & upload simetris hingga $speed mulai $price. $benefit. Pasang sekarang!",
            hashtags = listOf("#MyRepublic", "#InternetCepat", "#WiFiRumah", "#FiberOptic", "#AntiLag"),
            scenes = listOf(
                SceneItem(1, "MASALAH LAG", "Sering kesel gara-gara WiFi ngadat pas streaming atau main game bareng teman?", 3.0f, "#B91C1C"),
                SceneItem(2, "SOLUSI: $topic", "Saatnya beralih ke MyRepublic dengan koneksi full 100% fiber optic stabil.", 3.0f, "#4338CA"),
                SceneItem(3, "KECEPATAN $speed", "Kecepatan download dan upload simetris 1:1 hingga $speed tanpa buffering.", 3.0f, "#1D4ED8"),
                SceneItem(4, "HARGA: $price", "Dapatkan penawaran terbaik mulai $price! Ditambah benefit: $benefit.", 3.0f, "#047857"),
                SceneItem(5, "AMBIL PROMONYA", "Klik link di bio sekarang juga sebelum kuota promo pemasangan habis!", 3.0f, "#6D28D9")
            )
        )
    }

    private fun buildProductScript(topic: String, audience: String): ScriptResult {
        return ScriptResult(
            title = "Solusi Terbaik: $topic",
            hook = "Kamu masih pakai cara lama yang bikin ribet dan buang waktu?",
            cta = "Dapatkan diskon khusus hari ini via keranjang/bio!",
            caption = "Waktunya upgrade bersama $topic. Dirancang spesial untuk kebutuhan $audience.",
            hashtags = listOf("#RekomendasiProduk", "#ViralProduk", "#SpillProduk", "#SolusiPraktis"),
            scenes = listOf(
                SceneItem(1, "STOP CARA LAMA", "Masih pusing cari solusi harian yang awet, praktis, dan terpercaya?", 3.0f, "#B91C1C"),
                SceneItem(2, "KENALIN $topic", "Ini dia inovasi terbaru yang bakal mempermudah rutinitas harian $audience.", 3.0f, "#4338CA"),
                SceneItem(3, "KUALITAS UNGGUL", "Dibuat dengan bahan terbaik, berstandar tinggi, dan langsung terasa manfaatnya.", 3.0f, "#1D4ED8"),
                SceneItem(4, "KENAPA HARUS INI?", "Efektif, sangat praktis, dan ramah di kantong tanpa perlu repot.", 3.0f, "#047857"),
                SceneItem(5, "PROMO HARI INI", "Checkout sekarang juga sebelum stok promo eksklusif ini ludes!", 3.0f, "#6D28D9")
            )
        )
    }

    private fun buildEducationScript(topic: String, audience: String): ScriptResult {
        return ScriptResult(
            title = "Tips Singkat: $topic",
            hook = "Jangan sampai salah langkah saat kamu belajar $topic!",
            cta = "Simpan video ini biar nggak lupa saat praktek!",
            caption = "Trik mudah memahami $topic dalam 15 detik untuk $audience. Save dan bagikan!",
            hashtags = listOf("#Edukasi", "#TipsBerguna", "#BelajarCepat", "#TutorialPraktis"),
            scenes = listOf(
                SceneItem(1, "POIN UTAMA", "Banyak yang belum tahu trik penting seputar $topic. Simak sampai habis!", 3.0f, "#0F766E"),
                SceneItem(2, "LANGKAH PERTAMA", "Fokus pada fondasi paling dasar dan hindari kesalahan fatal yang sering terjadi.", 3.0f, "#0369A1"),
                SceneItem(3, "LANGKAH KEDUA", "Gunakan strategi yang konsisten dan terukur setiap hari secara teratur.", 3.0f, "#4338CA"),
                SceneItem(4, "HASIL NYATA", "Dalam hitungan minggu, kamu bakal merasakan efisiensi luar biasa.", 3.0f, "#15803D"),
                SceneItem(5, "SHARE & SAVE", "Follow akun ini untuk trik edukasi bermanfaat lainnya setiap hari!", 3.0f, "#7C3AED")
            )
        )
    }

    private fun buildStoryScript(topic: String, audience: String): ScriptResult {
        return ScriptResult(
            title = "Perjalanan: $topic",
            hook = "Jujur, awalnya saya hampir menyerah di titik terendah ini...",
            cta = "Tulis di komentar kalau kamu pernah ngalamin perjuangan serupa!",
            caption = "Perjalanan nyata menghadapi tantangan $topic. Semoga menginspirasi $audience!",
            hashtags = listOf("#Storytime", "#Motivasi", "#PengalamanNyata", "#KisahInspirasi"),
            scenes = listOf(
                SceneItem(1, "AWAL PERJUANGAN", "Awalnya nggak mudah memulai $topic dari nol tanpa modal dan pengalaman.", 3.0f, "#334155"),
                SceneItem(2, "TITIK BALIK", "Banyak rintangan dan kegagalan yang hampir bikin putus asa di tengah jalan.", 3.0f, "#991B1B"),
                SceneItem(3, "LANGKAH TEKUN", "Tapi kami terus berinovasi dan evaluasi sampai akhirnya nemu formula tepat.", 3.0f, "#1E40AF"),
                SceneItem(4, "BUAH KERJA KERAS", "Kini hasil nyata mulai terasa dan impian perlahan jadi kenyataan.", 3.0f, "#166534"),
                SceneItem(5, "JANGAN MENYERAH", "Kalau kamu sedang berjuang, tetap semangat! Giliran suksesmu pasti tiba.", 3.0f, "#581C87")
            )
        )
    }

    private fun buildSocialHookScript(topic: String, audience: String): ScriptResult {
        return ScriptResult(
            title = "Rahasia: $topic",
            hook = "Kalau kamu tahu trik $topic ini dari dulu, hidupmu bakal jauh lebih gampang!",
            cta = "Tag teman kamu yang butuh tahu informasi ini sekarang!",
            caption = "Trik viral seputar $topic yang jarang dibahas orang. Wajib tahu!",
            hashtags = listOf("#LifeHack", "#TrikViral", "#ViralTikTok", "#InfoPenting"),
            scenes = listOf(
                SceneItem(1, "STOP SCROLLING", "Berhenti scroll dulu! Ini satu hal tentang $topic yang bakal mengubah cara pandangmu.", 3.0f, "#B91C1C"),
                SceneItem(2, "FAKTA MENGEJUTKAN", "Kebanyakan orang membuang waktu dan biaya karena belum tahu rahasia ini.", 3.0f, "#4338CA"),
                SceneItem(3, "CARA KERJA CEPAT", "Cukup lakukan langkah sederhana ini dan lihat perubahannya dalam sekejap.", 3.0f, "#0284C7"),
                SceneItem(4, "BUKTI LANGSUNG", "Bukan sekadar teori, hasilnya langsung terlihat dan sangat memuaskan.", 3.0f, "#059669"),
                SceneItem(5, "SHARE KE TEMAN", "Kirim video ini ke teman atau grup kamu biar mereka juga terbantu!", 3.0f, "#7C3AED")
            )
        )
    }

    private fun buildShortAdScript(topic: String, audience: String): ScriptResult {
        return ScriptResult(
            title = "Promo Kilat: $topic",
            hook = "Cuma hari ini! Jangan lewatkan kesempatan langka ini!",
            cta = "Ketuk tombol di bawah sekarang!",
            caption = "Flash Sale $topic! Penawaran khusus terbatas untuk $audience!",
            hashtags = listOf("#FlashSale", "#PromoSpesial", "#DiskonHariIni", "#SerbaHemat"),
            scenes = listOf(
                SceneItem(1, "FLASH SALE", "Pemberitahuan khusus untuk kamu yang mau produk hemat maksimal!", 2.5f, "#991B1B"),
                SceneItem(2, topic.uppercase(), "Solusi terbaik berkualitas tinggi yang siap dikirim langsung hari ini.", 3.0f, "#1E3A8A"),
                SceneItem(3, "KUALITAS TERJAMIN", "Garansi resmi, pelayanan kilat, kepuasan terjamin 100%.", 3.0f, "#065F46"),
                SceneItem(4, "STOK TERBATAS", "Ketersediaan kuota promo terbatas hanya untuk 50 pemesan pertama!", 3.0f, "#854D0E"),
                SceneItem(5, "ORDER SEKARANG", "Klik tautan sekarang juga dan amankan diskon spesial kamu!", 3.5f, "#581C87")
            )
        )
    }
}
