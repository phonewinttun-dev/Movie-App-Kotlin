package com.movieapp

import android.app.Application

class MovieApplication : Application() {

    lateinit var database: com.movieapp.data.local.AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.movieapp.util.LocalizationManager.initialize(this)
        database = com.movieapp.data.local.AppDatabase.getInstance(this)
    }

    companion object {
        lateinit var instance: MovieApplication
            private set
    }
}
