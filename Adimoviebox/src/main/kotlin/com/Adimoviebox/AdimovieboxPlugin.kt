package com.Adimoviebox

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class AdimovieboxPlugin: Plugin() {
    override fun load(context: Context) {
        // Mendaftarkan provider Adimoviebox yang sudah di-update
        registerMainAPI(Adimoviebox())
    }
}
