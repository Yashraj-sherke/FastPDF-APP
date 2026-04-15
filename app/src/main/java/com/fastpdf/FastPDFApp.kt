package com.fastpdf

import android.app.Application

/**
 * Application class for FastPDF.
 * Kept minimal for Day 1 — will be used for DI setup (Hilt/Koin) in future.
 */
class FastPDFApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Future: Initialize dependency injection, analytics, crash reporting
    }
}
