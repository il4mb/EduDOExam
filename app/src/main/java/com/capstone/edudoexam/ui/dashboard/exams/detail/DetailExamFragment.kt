package com.capstone.edudoexam.ui.dashboard.exams.detail

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.capstone.edudoexam.components.BaseFragment
import com.capstone.edudoexam.databinding.FragmentExamDetailBinding
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.ui.dashboard.exams.detail.questions.QuestionsExamFragment
import com.capstone.edudoexam.ui.dashboard.exams.detail.studens.StudentsExamFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailExamFragment :
    BaseFragment<FragmentExamDetailBinding>(FragmentExamDetailBinding::class.java),
    TabLayout.OnTabSelectedListener {

    private val viewModel: DetailExamViewModel by viewModels()

    private val exam: Exam? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_EXAM, Exam::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_EXAM)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("DetailExamFragment", "onViewCreated: ${exam?.id}")

        isBottomNavigationVisible = false

        viewModel.exam.observe(viewLifecycleOwner) {
            binding.apply {
                examTitle.text = it.title
                examSubtitle.text = it.subTitle
                examCode.text = it.id.uppercase()
            }
        }

        binding.apply {
            viewPager.apply {
                adapter = ViewPagerAdapter(requireActivity(), exam?.id)

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        tabLayout.getTabAt(position)?.select()
                    }
                })
            }
            tabLayout.addOnTabSelectedListener(this@DetailExamFragment)
        }

        lifecycleScope.launch {
            delay(400)
            exam?.let { viewModel.setExam(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        setLoading(false)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when(tab?.position) {
            0 -> binding.viewPager.currentItem = 0
            1 -> binding.viewPager.currentItem = 1
        }
    }
    override fun onTabUnselected(tab: TabLayout.Tab?) {}
    override fun onTabReselected(tab: TabLayout.Tab?) {}
    class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        private var examId: String?
    ): FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> QuestionsExamFragment(examId)
                else -> StudentsExamFragment(examId)
            }
        }
    }

    companion object {
        const val ARG_EXAM = "exam"
    }
}