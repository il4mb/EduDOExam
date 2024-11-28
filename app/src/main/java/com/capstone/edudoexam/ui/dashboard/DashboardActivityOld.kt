package com.capstone.edudoexam.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.ModalBottom
import com.capstone.edudoexam.databinding.ActivityDashboardBinding
import com.capstone.edudoexam.databinding.ViewPopupLayoutBinding
import kotlin.math.abs

class DashboardActivityOld : AppCompatActivity() {

    private lateinit var startRotateAnim: Animation
    private lateinit var endRotateAnim: Animation

    private lateinit var binding: ActivityDashboardBinding

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

        binding = ActivityDashboardBinding.inflate(layoutInflater, null, false)
        startRotateAnim = AnimationUtils.loadAnimation(this, R.anim.start_rotate)
        endRotateAnim   = AnimationUtils.loadAnimation(this, R.anim.end_rotate)
        setContentView(binding.root)

        binding.apply {

            fragmentContainer.apply {
                adapter = ViewPagerAdapter(this@DashboardActivityOld)

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        toolbar.subtitle = null
                        menuLayout.profileButton.visibility = View.GONE
                        menuLayout.addButton.visibility = View.GONE
                        if(joinExamLayout.isVisible) {
                            joinExamLayout.apply {
                                pivotY = 0f
                                pivotX = width / 2f  // Center horizontally

                                animate()
                                    .setDuration(100)
                                    .alpha(0f)
                                    .scaleY(0f)
                                    .setInterpolator(AccelerateDecelerateInterpolator())
                                    .withEndAction {
                                        visibility = View.GONE // Hide after animation ends
                                    }
                                    .start()
                            }
                        }

                        when (position) {
                            0 -> {
                                bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
                                toolbar.title = getString(R.string.app_name)
                                toolbar.subtitle = getString(R.string.app_moto)
                                menuLayout.profileButton.visibility = View.VISIBLE
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

                            }
                            1 -> {
                                bottomNavigation.menu.findItem(R.id.nav_exams).isChecked = true
                                toolbar.title = "Exams"
                                menuLayout.addButton.visibility = View.VISIBLE
                            }
                            2 -> {
                                bottomNavigation.menu.findItem(R.id.nav_history).isChecked = true
                                toolbar.title = "History"
                            }
                            3 -> {
                                bottomNavigation.menu.findItem(R.id.nav_settings).isChecked = true
                                toolbar.title = "Settings"
                            }
                        }
                    }
                })

                fragmentContainer.setPageTransformer { page, position ->
                    val scale = 0.85f + (1 - abs(position)) * 0.15f
                    page.scaleX = scale
                    page.scaleY = scale
                    page.alpha = 0.5f + (1 - abs(position)) * 0.5f
                }
            }

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> fragmentContainer.currentItem = 0
                    R.id.nav_exams -> fragmentContainer.currentItem = 1
                    R.id.nav_history -> fragmentContainer.currentItem = 2
                    R.id.nav_settings -> fragmentContainer.currentItem = 3
                }
                true
            }

            menuLayout.profileButton.setOnClickListener {
                Navigation.findNavController(root).navigate(R.id.nav_profile)
            }
            menuLayout.apply {
                addButton.setOnClickListener { toggleAddMenu(it) }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.add_menu, menu)
        return true
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
            // animationStyle = R.style.popup_window_animation
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
}