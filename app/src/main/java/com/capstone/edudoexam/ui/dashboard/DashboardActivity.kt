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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.ModalBottom
import com.capstone.edudoexam.databinding.ActivityDashboardBinding
import com.capstone.edudoexam.databinding.ViewPopupLayoutBinding
import kotlin.math.abs

class DashboardActivity : AppCompatActivity() {

    private lateinit var startRotateAnim: Animation
    private lateinit var endRotateAnim: Animation

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater, null, false)
        startRotateAnim = AnimationUtils.loadAnimation(this, R.anim.start_rotate)
        endRotateAnim   = AnimationUtils.loadAnimation(this, R.anim.end_rotate)
        setContentView(binding.root)

        binding.apply {

            fragmentContainer.apply {
                adapter = ViewPagerAdapter(this@DashboardActivity)

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        menuLayout.profileButton.visibility = View.GONE
                        menuLayout.addButton.visibility = View.GONE

                        when (position) {
                            0 -> {
                                bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
                                toolbar.title = "Welcome to EduDoExam"
                                menuLayout.profileButton.visibility = View.VISIBLE
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
            animationStyle = R.style.popup_window_animation
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