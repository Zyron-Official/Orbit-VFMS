package com.zyron.orbit

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class App : Application() {
    companion object {        
        var instance: App? = null
    }
    override fun onCreate() {        
    super.onCreate()
        instance = this        
        Preferences.initialize(this)
        val MODE: Int = if (Preferences.isDarkModeEnabled()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(MODE)
       // if (Preferences.isDynamicColorsEnabled()) DynamicColors.applyToActivitiesIfAvailable(this)
    }
}