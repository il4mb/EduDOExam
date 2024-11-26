package com.capstone.edudoexam.ui.dashboard.settings

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.capstone.edudoexam.R
import com.capstone.edudoexam.ui.dashboard.SharedViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_dark_mode))?.let {
            it.setOnPreferenceChangeListener { _, newValue ->
                val isDarkMode = newValue as Boolean
                if (isDarkMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                true
            }
        }

        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_upcoming_notification))?.let {
            it.setOnPreferenceChangeListener { _, newValue ->

                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.topMargin.observe(viewLifecycleOwner) { marginTop ->
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin = marginTop
                view.layoutParams = this
            }
        }
    }
}