package com.capstone.edudoexam.ui.dashboard

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.AppBarLayout
import com.capstone.edudoexam.components.ModalBottom
import com.capstone.edudoexam.databinding.ActivityDashboard2Binding
import com.capstone.edudoexam.ui.dashboard.profile.ProfileFragment.UserBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    // private val userSession

    private val _binding: ActivityDashboard2Binding by lazy {
        ActivityDashboard2Binding.inflate(layoutInflater)
    }
    private lateinit var appBarInitialBg: Drawable
    private lateinit var sharedViewModel: SharedViewModel

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("UseSupportActionBar")
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val isDarkMode = sharedPref.getBoolean(getString(R.string.pref_dark_mode), false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

        appBarInitialBg = _binding.appBarLayout.background
        setContentView(_binding.root)
        setSupportActionBar(_binding.appBarLayout.toolbar)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val navView: BottomNavigationView = _binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_exams, R.id.nav_histories, R.id.nav_settings)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(this)

        _binding.apply {

            appBarLayout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val appBarHeight = _binding.appBarLayout.height
                sharedViewModel.updateTopMargin(appBarHeight)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {

        _binding.apply {

            resetUI()
            // TransitionManager.beginDelayedTransition(appBarLayout, ChangeBounds())
            appBarLayout.removeAllMenus()

            when (destination.id) {
                R.id.nav_home -> {
                    appBarLayout.title    = getString(R.string.app_name)
                    appBarLayout.subtitle = getString(R.string.app_moto)
                }
                else -> {
                    appBarLayout.title    = destination.label.toString()
                    appBarLayout.subtitle = ""
                }
            }
        }
    }

    private fun resetUI() {
       _binding.apply {
           if (!navView.isVisible) showNavBottom()
       }
    }

    fun addMenu(@DrawableRes icon: Int, @ColorInt color: Int = 0, onClick: (View) -> Unit) {
        _binding.appBarLayout.addMenu(icon, color, onClick)
    }

    fun hideNavBottom() {
        try {
            _binding.navView.apply {
                if(isShown) {
                    visibility = View.VISIBLE
                    alpha = 1f
                    animate()
                        .setDuration(80)
                        .alpha(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .withEndAction {
                            visibility = View.GONE
                        }
                        .start()
                }
            }
        } catch (_: Throwable) {

        }
    }

    fun showNavBottom() {
        try {
            _binding.navView.apply {
                if(!isShown) {
                    visibility = View.VISIBLE
                    alpha = 0f
                    animate()
                        .setDuration(200)
                        .alpha(1f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                }
            }
        } catch (_: Throwable) {}
    }

    fun getAppbar() : AppBarLayout {
        return _binding.appBarLayout
    }
}