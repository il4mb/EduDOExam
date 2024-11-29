package com.capstone.edudoexam.ui.dashboard.exams.detail

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.AppFragment
import com.capstone.edudoexam.databinding.FragmentExamDetailBinding
import com.capstone.edudoexam.ui.dashboard.exams.ExamsFragment
import com.capstone.edudoexam.ui.dashboard.exams.detail.questions.QuestionsExamFragment
import com.capstone.edudoexam.ui.dashboard.exams.detail.studens.StudentsExamFragment
import com.capstone.edudoexam.ui.dashboard.histories.HistoriesFragment
import com.capstone.edudoexam.ui.dashboard.home.HomeFragment
import com.capstone.edudoexam.ui.dashboard.settings.SettingsFragment
import com.google.android.material.tabs.TabLayout
import kotlin.math.abs

class DetailExamFragment :
    AppFragment<FragmentExamDetailBinding>(FragmentExamDetailBinding::class.java),
    TabLayout.OnTabSelectedListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewPager.apply {
                adapter = ViewPagerAdapter(requireActivity())

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        tabLayout.getTabAt(position)?.select()
                    }
                })
            }
            tabLayout.addOnTabSelectedListener(this@DetailExamFragment)
        }

        getParentActivity().hideNavBottom()
    }

    class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> QuestionsExamFragment()
                else -> StudentsExamFragment()
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when(tab?.position) {
            0 -> binding.viewPager.currentItem = 0
            1 -> binding.viewPager.currentItem = 1
        }
    }
    override fun onTabUnselected(tab: TabLayout.Tab?) {}
    override fun onTabReselected(tab: TabLayout.Tab?) {}
}