package com.Adicinemax21

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.metaproviders.TmdbProvider
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.SubtitleFile

class Adicinemax21 : TmdbProvider() {
    override var name = "Adicinemax21"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    
    // Link Resmi
    private val repoLink = "https://github.com/michat88/AdiXtream"
    private val warningTitle = "⚠️ KLIK SINI UNTUK UPDATE APLIKASI ⚠️"
    private val warningMessage = "Repo ini sudah mati. Klik link di bawah ini untuk download versi terbaru:\n\n$repoLink\n\n$repoLink"

    override val mainPage = mainPageOf(
        "data" to "Update Required"
    )

    // 1. Tampilkan "Fake Movie" di Halaman Utama
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val result = newMovieSearchResponse(warningTitle, repoLink, TvType.Movie) {
            this.posterUrl = "https://raw.githubusercontent.com/lagradost/CloudStream-3/master/app/src/main/ic_launcher-playstore.png" // Icon Cloudstream atau gambar warning
            this.plot = warningMessage
        }
        return newHomePageResponse(request.name, listOf(result))
    }

    // 2. Tampilkan Halaman Detail dengan Link yang BISA DIKLIK
    override suspend fun load(url: String): LoadResponse? {
        return newMovieLoadResponse(warningTitle, url, TvType.Movie, url) {
            this.posterUrl = "https://raw.githubusercontent.com/lagradost/CloudStream-3/master/app/src/main/ic_launcher-playstore.png"
            this.plot = warningMessage // Link di sini biasanya bisa diklik oleh user
            this.tags = listOf("Update", "Required", "AdiXtream")
        }
    }

    // 3. Blokir jika user mencoba memutar (Safety Net)
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        throw ErrorLoadingException("SILAHKAN UPDATE DULU! Buka: $repoLink")
    }

    // 4. Blokir Search
    override suspend fun search(query: String): List<SearchResponse>? {
        return listOf(
            newMovieSearchResponse(warningTitle, repoLink, TvType.Movie) {
                this.plot = warningMessage
            }
        )
    }
}
