package com.capstone.edudoexam.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.ModalBottom
import com.capstone.edudoexam.databinding.ActivityDashboard2Binding
import com.capstone.edudoexam.databinding.ViewPopupLayoutBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var startRotateAnim: Animation
    private lateinit var endRotateAnim: Animation
    private lateinit var binding: ActivityDashboard2Binding
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

        binding = ActivityDashboard2Binding.inflate(layoutInflater)
        startRotateAnim = AnimationUtils.loadAnimation(this, R.anim.start_rotate)
        endRotateAnim   = AnimationUtils.loadAnimation(this, R.anim.end_rotate)
        appBarInitialBg = binding.appBarLayout.background
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_exams, R.id.nav_histories, R.id.nav_settings)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(this)

        binding.apply {

            menuLayout.apply {
                addButton.setOnClickListener { toggleAddMenu(it) }
                profileButton.setOnClickListener {
                    navController.navigate(R.id.nav_profile)
                }
            }

            appBarLayout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val appBarHeight = binding.appBarLayout.height
                sharedViewModel.updateTopMargin(appBarHeight)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        binding.apply {
            resetUI()

            when (destination.id) {
                R.id.nav_home -> setupHomeUI()
                R.id.nav_exams -> setupExamsUI()
                R.id.nav_histories, R.id.nav_settings -> setupDefaultUI()
                else -> setupTransparentUI()
            }
        }
    }

    private fun setupTransparentUI() {

        binding.apply {
            lifecycleScope.launch {
                delay(80)
                appBarLayout.background = ColorDrawable(Color.TRANSPARENT)
                if (navView.isVisible) hideNavBottom()
            }
        }
    }

    private fun setupDefaultUI() {
       binding.apply {
           if (!navView.isVisible) showNavBottom()
       }
    }

    private fun setupExamsUI() {
        binding.apply {
            menuLayout.addButton.visibility = View.VISIBLE
            if (!navView.isVisible) showNavBottom()
        }
    }

    private fun setupHomeUI() {
        binding.apply {
            toolbar.title = getString(R.string.app_name)
            toolbar.subtitle = getString(R.string.app_moto)
            menuLayout.profileButton.visibility = View.VISIBLE

            // Show and animate joinExamLayout
            joinExamLayout.apply {
                visibility = View.VISIBLE
                scaleY = 0f
                alpha = 0f
                pivotY = 0f
                pivotX = width / 2f
                animate()
                    .setDuration(100)
                    .scaleY(1f)
                    .alpha(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }

            if (!navView.isVisible) showNavBottom()
        }
    }

    private fun resetUI() {
        binding.apply {
            // Reset toolbar and menu to the default state
            toolbar.subtitle = null
            menuLayout.profileButton.visibility = View.GONE
            menuLayout.addButton.visibility = View.GONE

            // Animate hiding the joinExamLayout if visible
            if (joinExamLayout.isVisible) {
                joinExamLayout.animate()
                    .setDuration(100)
                    .alpha(0f)
                    .scaleY(0f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction { joinExamLayout.visibility = View.GONE }
                    .start()
            }

            // Reset AppBar background
            appBarLayout.background = appBarInitialBg

            sharedViewModel.updateTopMargin(appBarLayout.height)
        }
    }

    @SuppressLint("ServiceCast")
    private fun toggleAddMenu(v: View) {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewPopupLayoutBinding.inflate(layoutInflater)
        val popUp = PopupWindow(this).apply {
            contentView = binding.root
            width = LinearLayout.LayoutParams.WRAP_CONTENT
            height = LinearLayout.LayoutParams.WRAP_CONTENT
            isFocusable = true
            //animationStyle = R.style.popup_window_animation
            setBackgroundDrawable(ColorDrawable())
        }

        val location = IntArray(2)
        v.getLocationOnScreen(location)

        val xOffset = 400
        val yOffset = 100

        popUp.showAtLocation(binding.root, Gravity.NO_GRAVITY, location[0] + xOffset, location[1] + yOffset)
        popUp.setOnDismissListener {
            v.startAnimation(endRotateAnim)
        }
        binding.actionCreate.setOnClickListener {
            popUp.dismiss()
            createExam()
        }

        binding.actionJoin.setOnClickListener {
            popUp.dismiss()
            joinExam()
        }

        v.startAnimation(startRotateAnim)
    }

    private fun createExam() {

        val modal = ModalBottom.create(R.layout.view_term_and_condition)
        Toast.makeText(this, "Create Exam selected", Toast.LENGTH_SHORT).show()
        modal.setAcceptHandler("Get it") { true }
        modal.show(supportFragmentManager, ModalBottom.TAG)
    }

    private fun joinExam() {

        Toast.makeText(this, "Join Exam selected", Toast.LENGTH_SHORT).show()
    }

    private fun hideNavBottom() {
        binding.navView.apply {
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

    private fun showNavBottom() {
        binding.navView.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate()
                .setDuration(200)
                .alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    fun getBinding(): ActivityDashboard2Binding {
        return binding
    }
}