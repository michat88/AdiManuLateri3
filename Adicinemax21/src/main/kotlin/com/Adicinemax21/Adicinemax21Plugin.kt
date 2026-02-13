package com.Adicinemax21

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import android.content.Context

@CloudstreamPlugin
class Adicinemax21Plugin: CloudstreamPlugin() {
    override fun load(context: Context) {
        // Mendaftarkan Provider Utama kita
        registerMainAPI(Adicinemax21())
    }
}
