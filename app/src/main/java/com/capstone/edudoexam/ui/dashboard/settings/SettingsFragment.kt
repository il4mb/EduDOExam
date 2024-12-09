package com.capstone.edudoexam.ui.dashboard.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.AppContextWrapper
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.dashboard.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val baseContext = requireActivity().baseContext

        setupSwitchPreference(
            key = "pref_dark_mode",
            onChange = { newValue ->
                val isDarkMode = newValue as Boolean
                if (isDarkMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                lifecycleScope.launch {
                    delay(600)
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
                    sharedPreferences.getString("pref_language", null)?.let {
                        updateLanguage(it)
                    }
                }
                requireActivity().recreate()
                true
            }
        )

        setupSwitchPreference(
            key = "pref_upcoming_notification",
            onChange = { _ -> true }
        )

        val languagePreference = findPreference<ListPreference>("pref_language")
        languagePreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                updateLanguage(newValue)
                true
            } else {
                false
            }
        }
    }

    private fun updateLanguage(languageCode: String) {
        val baseContext = requireActivity().baseContext

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        sharedPreferences.edit().putString("pref_language", languageCode).apply()

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()

        AppContextWrapper.setSystemLocale(config, locale)
        requireActivity().recreate()
    }

    private fun setupSwitchPreference(key: String, onChange: (newValue: Any) -> Boolean) {
        findPreference<SwitchPreferenceCompat>(key)?.setOnPreferenceChangeListener { _, newValue ->
            onChange(newValue)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                (requireActivity() as? DashboardActivity)?.apply {
                    showNavBottom()
                    configureAppBar()
                    setLoading(false)
                }
            }
        })
        sharedViewModel.topMargin.observe(viewLifecycleOwner) { marginTop ->
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin = marginTop
                view.layoutParams = this
            }
        }
    }
    private fun DashboardActivity.configureAppBar() {
        getAppbar().apply {
            removeAllMenus()
            removeAllContentViews()
        }
    }
}
