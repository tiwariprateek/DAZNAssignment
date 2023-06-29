package com.example.daznassignment

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DAZNApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}