package com.zyron.orbit

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.zyron.orbit.databinding.ActivityPreferencesBinding
import com.zyron.orbit.R
import kotlin.system.exitProcess

class PreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        title = "Preferences"
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Load Preferences Fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, PreferencesFragment())
            .commit()
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            val dynamicColorsSwitch = findPreference<SwitchPreferenceCompat>("dynamic_colors")
            val darkModeSwitch = findPreference<SwitchPreferenceCompat>("dark_mode")
            val recursiveExpansionSwitch = findPreference<SwitchPreferenceCompat>("recursive_expansion")
            val recursiveContractionSwitch = findPreference<SwitchPreferenceCompat>("recursive_contraction")
            val fileMapSwitch = findPreference<SwitchPreferenceCompat>("file_map")
            val layoutAnimationSwitch = findPreference<SwitchPreferenceCompat>("layout_animation")
            val itemAnimatorSwitch = findPreference<SwitchPreferenceCompat>("item_animator")
            val itemViewCachingSwitch = findPreference<SwitchPreferenceCompat>("item_view_caching")
            val recyclerItemViewSwitch = findPreference<SwitchPreferenceCompat>("recycler_item_view")

            // Reflect current state of dynamic colors from preferences
            dynamicColorsSwitch?.isChecked = Preferences.isDynamicColorsEnabled()
            darkModeSwitch?.isChecked = Preferences.isDarkModeEnabled()
            recursiveExpansionSwitch?.isChecked = Preferences.isRecursiveExpansionEnabled()
            recursiveContractionSwitch?.isChecked = Preferences.isRecursiveContractionEnabled()
            fileMapSwitch?.isChecked = Preferences.isFileMapEnabled()
            layoutAnimationSwitch?.isChecked = Preferences.isLayoutAnimationEnabled()
            itemAnimatorSwitch?.isChecked = Preferences.isItemAnimatorEnabled()
            itemViewCachingSwitch?.isChecked = Preferences.isItemViewCachingEnabled()
            recyclerItemViewSwitch?.isChecked = Preferences.isRecyclerItemViewEnabled()

            
      /*      dynamicColorsSwitch?.setOnPreferenceChangeListener { _, _ -> 
                App.instance?.let { 
                    if (Preferences.isDynamicColorsEnabled()) { 
                        Preferences.getPrefs().edit().putBoolean("dynamic_colors", false).apply() 
                        requireActivity().finish() 
                        DynamicColors.applyToActivitiesIfAvailable(it) 
                        MainActivity.instance?.recreate() 
                    } else { 
                        Preferences.getPrefs().edit().putBoolean("dynamic_colors", true).apply() 
                        requireActivity().finish() 
                        DynamicColors.applyToActivitiesIfAvailable(it.applicationContext as Application) 
                        MainActivity.instance?.recreate() 
                    } 
                } 
                true 
            }
            */
           
dynamicColorsSwitch?.setOnPreferenceChangeListener { _, _ -> 
    App.instance?.let { appInstance -> 
        val prefs = Preferences.getPrefs()
        val isDynamicColorsEnabled = Preferences.isDynamicColorsEnabled()
        
        
        requireActivity().finish()
        
        if (isDynamicColorsEnabled) {
            prefs.edit().putBoolean("dynamic_colors", false).apply()
            // Dynamic colors were enabled, now disabling
            DynamicColors.applyToActivitiesIfAvailable(appInstance)
           // 
            
        } else if (!isDynamicColorsEnabled) {
            prefs.edit().putBoolean("dynamic_colors", true).apply()
            // Dynamic colors were disabled, now enabling
            appInstance.setTheme(R.style.AppTheme)
           // DynamicColors.applyToActivitiesIfAvailable(appInstance.applicationContext as Application)
            
        } else {
            prefs.edit().putBoolean("dynamic_colors", true).apply()
            appInstance.setTheme(R.style.AppTheme)
          //  DynamicColors.applyToActivitiesIfAvailable(appInstance.applicationContext as Application)
        
        }
      //  MainActivity.instance?.finishAffinity()
        MainActivity.instance?.recreate()
    }
    true 
}

            darkModeSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isDarkModeEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("dark_mode", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("dark_mode", true).apply()
                }
                val MODE: Int = if (Preferences.isDarkModeEnabled()) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                requireActivity().finish()
                AppCompatDelegate.setDefaultNightMode(MODE)
                MainActivity.instance?.recreate()
                true
            }

            recursiveExpansionSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isRecursiveExpansionEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("recursive_expansion", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("recursive_expansion", true).apply()
                }
                MainActivity.instance?.recreate()
                true
            }

            recursiveContractionSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isRecursiveContractionEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("recursive_contraction", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("recursive_contraction", true).apply()
                }
                MainActivity.instance?.recreate()
                true
            }

            fileMapSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isFileMapEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("file_map", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("file_map", true).apply()
                }
                MainActivity.instance?.recreate()
                true
            }

            layoutAnimationSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isLayoutAnimationEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("layout_animation", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("layout_animation", true).apply()
                }
                MainActivity.instance?.recreate()
                true
            }

            itemAnimatorSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isItemAnimatorEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("item_animator", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("item_animator", true).apply()
                }
                MainActivity.instance?.recreate()
                true
            }

            itemViewCachingSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isItemViewCachingEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("item_view_caching", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("item_view_caching", true).apply()
                }
                MainActivity.instance?.recreate()
                true
            }

            recyclerItemViewSwitch?.setOnPreferenceChangeListener { _, _ ->
                if (Preferences.isRecyclerItemViewEnabled()) {
                    Preferences.getPrefs().edit().putBoolean("recycler_item_view", false).apply()
                } else {
                    Preferences.getPrefs().edit().putBoolean("recycler_item_view", true).apply()
                }
                MainActivity.instance?.recreate()
                true
            }
        }
    }
}