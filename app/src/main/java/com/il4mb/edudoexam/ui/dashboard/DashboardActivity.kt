package com.il4mb.edudoexam.ui.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.AppContextWrapper
import com.il4mb.edudoexam.components.NetworkStatusHelper
import com.il4mb.edudoexam.components.Utils
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.components.ui.AppBarLayout
import com.il4mb.edudoexam.components.ui.MenuLayout
import com.il4mb.edudoexam.databinding.ActivityDashboard2Binding
import com.il4mb.edudoexam.ui.LoadingHandler
import com.il4mb.edudoexam.ui.dashboard.histories.student.StudentResultViewModel
import com.il4mb.edudoexam.ui.exam.ExamActivity
import com.il4mb.edudoexam.ui.exam.ExamActivity.Companion.EXAM_ID


class DashboardActivity : AppCompatActivity(), NavController.OnDestinationChangedListener, LoadingHandler {

    private val examResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val navigateResult = result.data?.getStringExtra(ARGS_NAVIGATE_RESULT)
            navigateResult?.let {
                openDetailExamForStudent(it)
            }
        }
    }
    fun startExamActivity(examId: String) {
        val intent = Intent(this, ExamActivity::class.java).apply {
            putExtra(EXAM_ID, examId)
        }
        examResultLauncher.launch(intent)
    }

    private val _binding: ActivityDashboard2Binding by lazy {
        ActivityDashboard2Binding.inflate(layoutInflater)
    }
    private val sharedViewModel: SharedViewModel by viewModels()
    private val networkStatusHelper: NetworkStatusHelper by lazy {
        NetworkStatusHelper(this) { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    _binding.noConnectionLayout.apply {
                        animate()
                            .alpha(0f)
                            .withEndAction {
                                visibility = View.GONE
                            }
                            .duration = 400
                    }
                } else {
                    _binding.noConnectionLayout.apply {
                        visibility = View.VISIBLE
                        alpha = 0f
                        animate()
                            .alpha(1f)
                            .duration = 400
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        networkStatusHelper.startListening()
    }

    override fun onStop() {
        super.onStop()
        networkStatusHelper.stopListening()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("UseSupportActionBar", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(_binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(_binding.navView) { v, windowInsets  ->
            v.setPadding(0, 0, 0, 0)
            windowInsets
        }
        setContentView(_binding.root)

        setSupportActionBar(_binding.appBarLayout.toolbar)

        val navView: BottomNavigationView = _binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_exams, R.id.nav_histories, R.id.nav_settings)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(this)

        _binding.apply {
            noConnectionLayout.setOnTouchListener{ _, _ -> true }
            // appBarLayout.inflateMenu(R.menu.add_menu)
        }
        sharedViewModel.fetchUser(this) {}

        if(!Utils.isInternetAvailable(this)) {
            _binding.noConnectionLayout.visibility = View.VISIBLE
        }

    }

    private val studentResultLiveData: StudentResultViewModel by viewModels()
    private fun openDetailExamForStudent(examId: String) {
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)
        sharedViewModel.user.value?.let { user ->
            try {
                studentResultLiveData.loadData(
                    activity = this,
                    examId   = examId,
                    user     = user,
                    succeed  = {
                        navController.navigate(R.id.nav_student_result)
                    },
                    failed   = {
                        showInfo(it.message)
                    }
                )
            } catch (t:Throwable) {
                t.printStackTrace()
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

    fun addMenu(@DrawableRes icon: Int, @ColorInt color: Int = 0, onClick: (View) -> Unit): MenuLayout.MenuItem {
        return _binding.appBarLayout.addMenu(icon, color, onClick)
    }
    fun addMenu(menuItem: MenuLayout.MenuItem) {
        _binding.appBarLayout.addMenu(menuItem)
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

    @SuppressLint("ClickableViewAccessibility")
    override fun setLoading(isLoading: Boolean) {
        // Block touch events when loading
        _binding.loadingLayout.root.setOnTouchListener { _, _ -> isLoading }

        // Check if the current visibility is the same as the desired state, return early if true
        if (isLoading && _binding.loadingLayout.root.visibility == View.VISIBLE) return
        if (!isLoading && _binding.loadingLayout.root.visibility == View.GONE) return

        // Declare a flag to prevent hiding the loading indicator if it is shown again
        val hideRunnable = Runnable {
            // Only hide if the loading state is false and the view is still visible
            if (!isLoading && _binding.loadingLayout.root.visibility == View.VISIBLE) {
                // Animate out with a delay
                _binding.loadingLayout.loadingIndicatorContainer.apply {
                    animate()
                        .setDuration(200)
                        .alpha(0f)
                        .scaleX(2f)
                        .scaleY(2f)
                        .translationX(0.5f)
                        .translationY(0.5f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .withEndAction {
                            // Set visibility to GONE after animation and ensure no redundant animations
                            _binding.loadingLayout.root.visibility = View.GONE
                        }
                        .start()
                }
            }
        }

        if (isLoading && _binding.loadingLayout.root.visibility != View.VISIBLE) {
            _binding.loadingLayout.root.visibility = View.VISIBLE
            _binding.loadingLayout.loadingIndicatorContainer.apply {
                alpha = 0f
                scaleX = 2f
                scaleY = 2f
                translationX = 0.5f
                translationY = 0.5f

                animate()
                    .setDuration(200)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0f)
                    .translationY(0f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withStartAction {
                        _binding.loadingLayout.root.isClickable = false
                    }
                    .withEndAction {
                        _binding.loadingLayout.root.isClickable = true
                    }
                    .start()
            }

            Handler(Looper.getMainLooper()).removeCallbacks(hideRunnable)

        } else if (!isLoading && _binding.loadingLayout.root.visibility == View.VISIBLE) {
            Handler(Looper.getMainLooper()).postDelayed(hideRunnable, 400)
        }
    }

    fun getAppbar() : AppBarLayout {
        return _binding.appBarLayout
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            val language = PreferenceManager.getDefaultSharedPreferences(newBase).getString("pref_language", "en") ?: "en"
            super.attachBaseContext(AppContextWrapper.wrap(it, language))
        } ?: run {
            super.attachBaseContext(newBase)
        }

    }

    private fun showInfo(message: String) {
        InfoDialog(this).setMessage(message).show()
    }

    companion object {
        const val ARGS_NAVIGATE_RESULT = "args-navigate-result"
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.fetchUser(this) {}
    }
}