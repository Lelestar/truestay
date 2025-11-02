package ca.uqac.inf865.truestay

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrueStayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Global initializations if necessary
    }
}