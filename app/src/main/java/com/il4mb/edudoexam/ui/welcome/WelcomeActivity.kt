package com.il4mb.edudoexam.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.il4mb.edudoexam.api.AuthInterceptor
import com.il4mb.edudoexam.components.AppContextWrapper
import com.il4mb.edudoexam.databinding.ActivityWelcomeBinding
import com.il4mb.edudoexam.ui.LoadingHandler
import com.il4mb.edudoexam.ui.dashboard.DashboardActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity(), LoadingHandler {

    private val _binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        if(AuthInterceptor.getToken(this) != null) {
            lifecycleScope.launch {
                delay(200)
                startActivity(Intent(this@WelcomeActivity, DashboardActivity::class.java))
                finish()
            }
        }

        super.onCreate(savedInstanceState)

        setContentView(_binding.root)

        lifecycleScope.launch {
            delay(400)
            setLoading(false)
        }
    }

    override fun setLoading(isLoading: Boolean) {
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

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            val language = PreferenceManager.getDefaultSharedPreferences(newBase).getString("pref_language", "en") ?: "en"
            super.attachBaseContext(AppContextWrapper.wrap(it, language))
        } ?: run {
            super.attachBaseContext(newBase)
        }

    }

}