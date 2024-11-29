package com.capstone.edudoexam.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.databinding.ActivityWelcomeBinding
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {

    private val _binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val isDarkMode = sharedPref.getBoolean(getString(R.string.pref_dark_mode), false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(_binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setLoading(false)

    }

    fun goToDashboard(view: View?) {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            _binding.loadingLayout.root.visibility = View.VISIBLE
            _binding.root.children.forEach {
                it.isEnabled = false
            }
        } else {
            _binding.loadingLayout.root.visibility = View.GONE
            _binding.root.children.forEach {
                it.isEnabled = true
            }
        }
    }
}