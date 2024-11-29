package com.capstone.edudoexam.ui.dashboard.settings

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.capstone.edudoexam.R
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.dashboard.SharedViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        setupSwitchPreference(
            key = getString(R.string.pref_dark_mode),
            onChange = { newValue ->
                val isDarkMode = newValue as Boolean
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                true
            }
        )

        setupSwitchPreference(
            key = getString(R.string.pref_upcoming_notification),
            onChange = { newValue ->
                // Add logic to handle notification preferences here
                true
            }
        )
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
