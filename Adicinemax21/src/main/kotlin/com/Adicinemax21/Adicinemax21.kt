package com.Adicinemax21

// Import standar yang wajib ada agar tidak error
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.metaproviders.TmdbProvider
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.SubtitleFile

class Adicinemax21 : TmdbProvider() {
    // --- Identitas Provider ---
    override var name = "Adicinemax21"
    override val hasMainPage = true
    override var lang = "id"
    
    // --- Syarat Wajib TmdbProvider (Agar tidak error saat compile) ---
    // Kita harus tetap mendefinisikan ini, meski tidak dipakai
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    
    // Kita buat mainPage kosong agar memenuhi syarat variabel
    override val mainPage = mainPageOf(
        "data" to "Update Required"
    )

    // --- PESAN KILL SWITCH ---
    private val killSwitchMessage = "⚠️ REPO INI SUDAH MATI. SILAHKAN DOWNLOAD & UPDATE APLIKASI ADIXTREAM KE VERSI TERBARU UNTUK LANJUT MENONTON! ⚠️"

    // --- FUNGSI PEMBLOKIRAN ---

    // 1. Blokir Halaman Utama (Home)
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        throw ErrorLoadingException(killSwitchMessage)
    }

    // 2. Blokir Halaman Detail (Saat klik poster)
    override suspend fun load(url: String): LoadResponse? {
        throw ErrorLoadingException(killSwitchMessage)
    }

    // 3. Blokir Pemutar Video (Saat klik play)
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        throw ErrorLoadingException(killSwitchMessage)
    }

    // 4. Blokir Pencarian
    override suspend fun search(query: String): List<SearchResponse>? {
        throw ErrorLoadingException(killSwitchMessage)
    }
}
