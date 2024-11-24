package com.capstone.edudoexam.ui.dashboard.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.capstone.edudoexam.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}