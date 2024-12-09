package com.capstone.edudoexam.ui.dashboard.exams.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils.Companion.getAttr
import com.capstone.edudoexam.databinding.FragmentExamDetailBinding
import com.capstone.edudoexam.ui.dashboard.exams.ExamsViewModel
import com.capstone.edudoexam.ui.dashboard.exams.detail.config.ExamConfigFragment
import com.capstone.edudoexam.ui.dashboard.exams.detail.questions.QuestionsExamFragment
import com.capstone.edudoexam.ui.dashboard.exams.detail.studens.StudentsExamFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailExamFragment :
    BaseFragment<FragmentExamDetailBinding>(FragmentExamDetailBinding::class.java),
    TabLayout.OnTabSelectedListener {

    private val detailViewModel: DetailExamViewModel by activityViewModels()
    private val fragmentList: List<Fragment> by lazy {
        listOf(
            StudentsExamFragment.newInstance(),
            QuestionsExamFragment.newInstance()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liveCycleObserve()
        setupUI()
    }

    private fun liveCycleObserve() {
        detailViewModel.exam.observe(viewLifecycleOwner) {
            binding.apply {
                examTitle.text = it.title
                examSubtitle.text = it.subTitle
                examCode.text = it.id.uppercase()
                if(it.isOngoing) {
                    setupOngoingUI()
                    finishExamButton.apply {
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupOngoingUI() {
        binding.apply {
            ongoingWarningInfoContainer.visibility = View.VISIBLE
        }
    }

    private fun setupUI() {
        binding.apply {
            viewPager.apply {
                adapter = ViewPagerAdapter(requireActivity(), fragmentList)

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
            getParentActivity().apply {
                addMenu(R.drawable.baseline_settings_24, getAttr(requireContext(), android.R.attr.textColor)) {
                    val examId = detailViewModel.exam.value?.id
                    if (examId.isNullOrEmpty()) return@addMenu
                    findNavController().navigate(R.id.action_nav_exam_detail_to_nav_exam_config, Bundle().apply {
                        putString(ExamConfigFragment.ARG_EXAM_ID, examId)
                    })
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        try {

            fragmentList.forEach { fragment ->
                if (fragment is StudentsExamFragment && fragment.isAdded) {
                    fragment.doFetchUsers()
                }
                if (fragment is QuestionsExamFragment && fragment.isAdded) {
                    fragment.doFetchQuestions()
                }
            }

        } catch (e: Exception) {
            Log.e("DetailExamFragment", "onResume: ", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detailViewModel.clearAll()
    }

    private fun onErrorHandler(e: Response) {
        lifecycleScope.launch {
            delay(400)
            setLoading(false)
            Snackbar.with(binding.root).show("Something went wrong", e.message, Snackbar.LENGTH_LONG)
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
    class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        private var fragmentList: List<Fragment>
    ): FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = fragmentList.size
        override fun createFragment(position: Int): Fragment = fragmentList[position]
    }
}