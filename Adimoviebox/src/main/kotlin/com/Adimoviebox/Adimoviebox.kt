package com.Adimoviebox

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import java.util.TimeZone

class Adimoviebox : MainAPI() {
    override var mainUrl = "https://moviebox.ph"
    // ✅ FIX 1: Host API disesuaikan dengan Log Network (h5-api.aoneroom.com)
    private val apiHost = "https://h5-api.aoneroom.com"
    
    override var name = "Adimoviebox"
    override val hasMainPage = true
    override var lang = "en"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime,
        TvType.AsianDrama
    )

    // Deteksi zona waktu otomatis agar konten sinkron
    private val deviceTimezone: String 
        get() = TimeZone.getDefault().id

    // ✅ FIX 2: Header menggunakan Token Baru & User-Agent yang sesuai
    private val commonHeaders: Map<String, String>
        get() = mapOf(
            "Authorization" to "Bearer EyJhbGciOiJlUzI1NilsinR5cCl6lkpXVCJ9.eyJ1aWQiOjc4NDAwNzU0NTU2MTIyMDk4MTYsImF0cC16MywiZXh0IjoiMTc2NzUzMDI00SIsImV4cCl6MTc3NTMwNjI00SwiaWF0ljoxNzY3NTI5OTQ5fQ.9tF50rKtU-mawbeyDCoyBwgamN6ku0CMnv99Rf8BhxY", 
            "X-Client-Info" to "{\"timezone\":\"$deviceTimezone\"}",
            "Referer" to "$mainUrl/",
            "Origin" to mainUrl,
            "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Mobile Safari/537.36",
            "X-Request-Lang" to "en"
        )

    override val mainPage: List<MainPageData> = mainPageOf(
        "$apiHost/wefeed-h5api-bff/home?host=moviebox.ph" to "Home",
        "$apiHost/wefeed-h5api-bff/subject/trending?page=0&perPage=18" to "Trending Now",
        "5,Korea,All" to "K-Drama",
        "2,Indonesia,All" to "Indo Film",
        "5,China,All" to "C-Drama",
        "5,All,Anime" to "Anime"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = mutableListOf<SearchResponse>()
        try {
            if (request.data.startsWith("http")) {
                val response = app.get(request.data, headers = commonHeaders).text
                val json = parseJson<MediaResponse>(response)
                
                // ✅ FIX 3: Filter pintar untuk mengambil Film & Banner saja, membuang Iklan/Kursus/Bola
                json.data?.operatingList?.forEach { op ->
                    if (op.type == "SUBJECTS_MOVIE") {
                        op.subjects?.forEach { items.add(it.toSearchResponse(this)) }
                    } else if (op.type == "BANNER") {
                        op.banner?.items?.forEach { bannerItem ->
                            // Banner items kadang memiliki objek 'subject' di dalamnya
                            val subject = bannerItem.subject ?: bannerItem
                            if (subject.subjectId != "0") { // Filter dummy item
                                items.add(subject.toSearchResponse(this))
                            }
                        }
                    }
                }
                json.data?.subjectList?.forEach { items.add(it.toSearchResponse(this)) }
            } else {
                // Logika Filter Kategori
                val params = request.data.split(",")
                val body = mapOf(
                    "tabId" to params[0],
                    "page" to page.toString(),
                    "perPage" to "20",
                    "filterType" to mapOf("country" to params[1], "genre" to params[2], "sort" to "Hottest", "year" to "All").toJson()
                )
                // ✅ FIX 4: Path filter yang benar (/subject/filter)
                val response = app.post("$apiHost/wefeed-h5api-bff/subject/filter", headers = commonHeaders, json = body).parsedSafe<MediaResponse>()
                response?.data?.items?.forEach { items.add(it.toSearchResponse(this)) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return newHomePageResponse(request.name, items.distinctBy { it.name })
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val body = mapOf("keyword" to query, "page" to "1", "perPage" to "20", "subjectType" to "0")
        return app.post("$apiHost/wefeed-h5api-bff/subject/search", headers = commonHeaders, json = body)
            .parsedSafe<MediaResponse>()?.data?.items?.map { it.toSearchResponse(this) } ?: emptyList()
    }

    override suspend fun load(url: String): LoadResponse {
        val id = url.substringAfterLast("/")
        // ✅ FIX 5: Path Detail yang benar (/subject/detail) dan Header Wajib
        val response = app.get("$apiHost/wefeed-h5api-bff/subject/detail?subjectId=$id", headers = commonHeaders)
        
        val res = response.parsedSafe<MediaDetailResponse>()?.data 
            ?: throw ErrorLoadingException("Data Null: Cek Token atau Koneksi")

        val subject = res.subject
        val isTv = subject?.subjectType == 2

        return if (isTv) {
            val episodes = res.resource?.seasons?.flatMap { season ->
                // Parsing episode yang lebih aman
                val eps = season.allEp?.split(",")?.mapNotNull { it.toIntOrNull() } 
                    ?: (1..(season.maxEp ?: 0)).toList()
                
                eps.map { epNum ->
                    newEpisode(LoadData(id, season.se, epNum, subject?.detailPath).toJson()) {
                        this.season = season.se
                        this.episode = epNum
                    }
                }
            } ?: emptyList()
            newTvSeriesLoadResponse(subject?.title ?: "No Title", url, TvType.TvSeries, episodes) {
                fillDetails(this, subject)
            }
        } else {
            newMovieLoadResponse(subject?.title ?: "No Title", url, TvType.Movie, LoadData(id, detailPath = subject?.detailPath).toJson()) {
                fillDetails(this, subject)
            }
        }
    }

    private fun fillDetails(container: LoadResponse, item: Items?) {
        // ✅ FIX 6: Null Safety untuk gambar agar tidak crash (Coil Error)
        container.posterUrl = item?.cover?.url?.takeIf { it.isNotEmpty() } 
            ?: item?.image?.url // Fallback ke image jika cover null
        
        container.plot = if (item?.description.isNullOrBlank()) "No description available." else item?.description
        container.year = item?.releaseDate?.substringBefore("-")?.toIntOrNull()
        container.score = Score.from10(item?.imdbRatingValue)
        container.recommendations = null // Bisa diisi jika ada endpoint rekomendasi
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val media = parseJson<LoadData>(data)
        // ✅ FIX 7: Path Play yang benar (/subject/play)
        val response = app.get("$apiHost/wefeed-h5api-bff/subject/play?subjectId=${media.id}&se=${media.season ?: 0}&ep=${media.episode ?: 0}", headers = commonHeaders)
            .parsedSafe<MediaResponse>()?.data

        response?.streams?.forEach { stream ->
            callback.invoke(
                newExtractorLink(
                    this.name, 
                    this.name, 
                    stream.url ?: return@forEach, 
                    this.mainUrl, 
                    getQualityFromName(stream.resolutions)
                )
            )
        }
        return true
    }
}

// --- Data Models Sesuai Struktur JSON Real ---

data class LoadData(val id: String?, val season: Int? = null, val episode: Int? = null, val detailPath: String?)

data class MediaResponse(@JsonProperty("data") val data: Data? = null) {
    data class Data(
        @JsonProperty("operatingList") val operatingList: List<OperatingItem>? = null,
        @JsonProperty("subjectList") val subjectList: List<Items>? = null,
        @JsonProperty("items") val items: List<Items>? = null,
        @JsonProperty("streams") val streams: List<Stream>? = null
    )
}

data class OperatingItem(
    @JsonProperty("type") val type: String? = null,
    @JsonProperty("subjects") val subjects: List<Items>? = null,
    @JsonProperty("banner") val banner: Banner? = null
)

data class Banner(@JsonProperty("items") val items: List<Items>? = null)

data class Stream(
    @JsonProperty("url") val url: String? = null,
    @JsonProperty("resolutions") val resolutions: String? = null,
    @JsonProperty("format") val format: String? = null
)

data class MediaDetailResponse(@JsonProperty("data") val data: Data? = null) {
    data class Data(
        @JsonProperty("subject") val subject: Items? = null,
        @JsonProperty("resource") val resource: Resource? = null
    ) {
        data class Resource(
            @JsonProperty("seasons") val seasons: List<Season>? = null
        )
        data class Season(
            @JsonProperty("se") val se: Int? = null, 
            @JsonProperty("maxEp") val maxEp: Int? = null,
            @JsonProperty("allEp") val allEp: String? = null
        )
    }
}

data class Items(
    @JsonProperty("subjectId") val subjectId: String? = null,
    @JsonProperty("subjectType") val subjectType: Int? = null,
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("releaseDate") val releaseDate: String? = null,
    @JsonProperty("cover") val cover: Cover? = null,
    @JsonProperty("image") val image: Cover? = null, // Tambahan untuk item banner
    @JsonProperty("imdbRatingValue") val imdbRatingValue: String? = null,
    @JsonProperty("detailPath") val detailPath: String? = null,
    // Field khusus banner yang mungkin membungkus subject asli
    @JsonProperty("subject") val subject: Items? = null 
) {
    fun toSearchResponse(api: MainAPI): SearchResponse {
        // Jika ini adalah item banner yang membungkus subject asli, gunakan yang di dalam
        val item = if (subject != null && subject.subjectId != null) subject else this
        
        return api.newMovieSearchResponse(
            item.title ?: "Unknown",
            "${api.mainUrl}/detail/${item.subjectId}",
            if (item.subjectType == 2) TvType.TvSeries else TvType.Movie,
            false
        ) { 
            this.posterUrl = item.cover?.url ?: item.image?.url 
        }
    }
    data class Cover(@JsonProperty("url") val url: String?)
}
