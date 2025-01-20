package com.zyron.orbit

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object Preferences {
    private lateinit var preferences: SharedPreferences

    fun initialize(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getPrefs(): SharedPreferences {
        return preferences
    }

    fun isDynamicColorsEnabled(): Boolean {
        return preferences.getBoolean("dynamic_colors", false)
    }

    fun isDarkModeEnabled(): Boolean {
        return preferences.getBoolean("dark_mode", false)
    }

    fun isRecursiveExpansionEnabled(): Boolean {
        return preferences.getBoolean("recursive_expansion", false)
    }

    fun isRecursiveContractionEnabled(): Boolean {
        return preferences.getBoolean("recursive_contraction", false)
    }

    fun isFileMapEnabled(): Boolean {
        return preferences.getBoolean("file_map", false)
    }

    fun isLayoutAnimationEnabled(): Boolean {
        return preferences.getBoolean("layout_animation", false)
    }

    fun isItemAnimatorEnabled(): Boolean {
        return preferences.getBoolean("item_animator", false)
    }

    fun isItemViewCachingEnabled(): Boolean {
        return preferences.getBoolean("item_view_caching", false)
    }

    fun isRecyclerItemViewEnabled(): Boolean {
        return preferences.getBoolean("recycler_item_view", false)
    }
}