package com.il4mb.edudoexam.ui.dashboard.exams.detail

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
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.Utils.Companion.getAttr
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.databinding.FragmentExamDetailBinding
import com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.QuestionsExamFragment
import com.il4mb.edudoexam.ui.dashboard.exams.detail.studens.StudentsExamFragment
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
        detailViewModel.apply {
            exam.observe(viewLifecycleOwner) {
                binding.apply {
                    examTitle.text = it.title
                    examSubtitle.text = it.subTitle
                    examCode.text = it.id
                    if(it.isOngoing) {
                        setupOngoingUI()
                    } else if(it.finishAt.time < System.currentTimeMillis()) {
                        setupFinishedUI()
                    }
                }
            }
        }
    }

    private fun setupFinishedUI() {
        binding.apply {
            ongoingInfoContainer.visibility = View.GONE
            finishedInfoContainer.visibility = View.VISIBLE
        }
    }
    private fun setupOngoingUI() {
        binding.apply {
            ongoingInfoContainer.visibility = View.VISIBLE
            finishedInfoContainer.visibility = View.GONE
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
                    detailViewModel.exam.value?.id?.let { examId ->
                        detailViewModel.fetchBlockedParticipants(
                            activity = requireActivity(),
                            examId = examId,
                            success =  {
                                findNavController().navigate(R.id.action_nav_exam_detail_to_nav_exam_config)
                           },
                            error = {
                                showInfo(getString(R.string.something_went_wrong), it.message)
                            })
                    } ?: run {
                        showInfo(getString(R.string.missing_exam_id))
                    }
                }
            }
        }
    }

    private fun showInfo(message: String) {
        InfoDialog(requireActivity())
            .setMessage(message)
            .show()
    }

    private fun showInfo(title: String, message: String) {
        InfoDialog(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .show()
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