package com.Adicinemax21

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin // <--- INI YANG PENTING
import android.content.Context

@CloudstreamPlugin
class Adicinemax21Plugin: Plugin() { // <--- Ubah "CloudstreamPlugin" jadi "Plugin"
    override fun load(context: Context) {
        // Mendaftarkan Provider Utama
        registerMainAPI(Adicinemax21())
    }
}
