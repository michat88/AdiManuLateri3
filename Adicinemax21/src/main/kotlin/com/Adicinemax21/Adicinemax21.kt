package com.lagradost.cloudstream3.movieproviders

import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.ExtractorLink
import com.lagradost.cloudstream3.SubtitleFile

// Pastikan nama class ini sama persis dengan yang ada di file aslinya
class Adicinemax21Provider : MainAPI() {

    // Identitas Provider (Boleh dibiarkan default atau disesuaikan)
    override var mainUrl = "https://adicinemax21.com" 
    override var name = "Adicinemax21"
    override val hasMainPage = true
    override var lang = "id"

    // Pesan Error Kill Switch
    private val killSwitchMessage = "⚠️ REPO INI SUDAH MATI. SILAHKAN DOWNLOAD & UPDATE APLIKASI ADIXTREAM KE VERSI TERBARU UNTUK LANJUT MENONTON! ⚠️"

    // 1. Blokir saat user klik poster film
    override suspend fun load(url: String): LoadResponse? {
        throw ErrorLoadingException(killSwitchMessage)
    }

    // 2. Blokir saat user klik tombol putar/server
    override suspend fun loadLinks(
        data: String,
        isDataJob: Boolean,
        callback: (ExtractorLink) -> Unit,
        subtitleCallback: (SubtitleFile) -> Unit
    ): Boolean {
        throw ErrorLoadingException(killSwitchMessage)
    }
}
