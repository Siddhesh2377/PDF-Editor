package com.example.pdfeditor

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        PDFBoxResourceLoader.init(this)
    }
}
