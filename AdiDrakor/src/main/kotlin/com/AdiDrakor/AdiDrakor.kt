package com.AdiDrakor

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.metaproviders.TmdbProvider
import com.lagradost.cloudstream3.utils.ExtractorLink

class AdiDrakor : TmdbProvider() {
    // --- Identitas Provider ---
    override var name = "AdiDrakor"
    override val hasMainPage = true
    override var lang = "id"

    // --- Syarat Wajib TmdbProvider ---
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

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

    // 2. Blokir Halaman Detail
    override suspend fun load(url: String): LoadResponse? {
        throw ErrorLoadingException(killSwitchMessage)
    }

    // 3. Blokir Pemutar Video
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
