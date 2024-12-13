package com.il4mb.edudoexam.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.il4mb.edudoexam.ui.dashboard.exams.ExamsFragment
import com.il4mb.edudoexam.ui.dashboard.histories.HistoriesFragment
import com.il4mb.edudoexam.ui.dashboard.home.HomeFragment
import com.il4mb.edudoexam.ui.dashboard.settings.SettingsFragment

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