package com.capstone.edudoexam.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.capstone.edudoexam.ui.dashboard.exams.ExamsFragment
import com.capstone.edudoexam.ui.dashboard.histories.HistoriesFragment
import com.capstone.edudoexam.ui.dashboard.home.HomeFragment
import com.capstone.edudoexam.ui.dashboard.settings.SettingsFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> ExamsFragment()
            2 -> HistoriesFragment()
            3 -> SettingsFragment()
            else -> HomeFragment()
        }
    }
}