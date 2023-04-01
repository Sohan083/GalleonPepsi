package com.example.galleonpepsi.core

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    override fun onCreate() {
        APP_CONTEXT = this
        super.onCreate()
    }

    companion object {
      lateinit var  APP_CONTEXT: Context
    }
}
