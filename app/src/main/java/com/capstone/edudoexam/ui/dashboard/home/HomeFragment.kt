package com.capstone.edudoexam.ui.dashboard.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Utils
import com.capstone.edudoexam.components.Utils.Companion.asEstimateTime
import com.capstone.edudoexam.components.Utils.Companion.asLocalDateTime
import com.capstone.edudoexam.components.Utils.Companion.hideKeyboard
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.FragmentHomeBinding
import com.capstone.edudoexam.databinding.ViewItemExamBinding
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import com.capstone.edudoexam.ui.dashboard.histories.student.ExamStudentResultFragment.Companion.EXAM_ID
import com.capstone.edudoexam.ui.exam.ExamActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment :
    BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::class.java),
    GenericListAdapter.ItemBindListener<Exam, ViewItemExamBinding> {

    private val ongoingListAdapter: GenericListAdapter<Exam, ViewItemExamBinding> by lazy {
        GenericListAdapter(
            ViewItemExamBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
    }
    private val upcomingListAdapter: GenericListAdapter<Exam, ViewItemExamBinding> by lazy {
        GenericListAdapter(
            ViewItemExamBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
    }
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBottomNavigationVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        liveCycleObserve()

        lifecycleScope.launch {
            delay(600)
            getParentActivity().apply {
                showNavBottom()
                addMenu(R.drawable.baseline_person_24) {
                    findNavController().navigate(R.id.action_nav_home_to_nav_profile)
                }
            }
            fetchUpcomingExams()
        }
    }

    private fun setupUI() {
        binding.apply {
            recyclerViewUpcoming.apply {
                adapter = upcomingListAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            recyclerViewOngoing.apply {
                adapter = ongoingListAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun liveCycleObserve() {
        viewModel.apply {
            upcomingExams.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    if(it.isNotEmpty()) {
                        binding.upcomingEmptyState.visibility = View.GONE
                    }
                    upcomingListAdapter.submitList(it)
                    setLoading(false)
                }
            }
            ongoingExams.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    if(it.isNotEmpty()) {
                        binding.ongoingWrapper.visibility = View.VISIBLE
                    } else {
                        binding.ongoingWrapper.visibility = View.GONE
                    }
                    ongoingListAdapter.submitList(it)
                    setLoading(false)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {

        binding.apply {
            codeTextView.text = item.id
            titleView.text    = item.title
            subtitleView.text = item.subTitle
            if(item.isAnswered) {
                dateTime.apply {
                    text = context.getString(R.string.answered)
                    setTextColor(context.getColor(R.color.primary_200))
                    typeface = resources.getFont(R.font.montserrat_bold)
                }
                deckCard.visibility = View.GONE
            } else if(item.isOngoing) {
                ongoingBadged.visibility = View.VISIBLE
                dateTime.apply {
                    text = context.getString(R.string.ongoing)
                    setTextColor(context.getColor(R.color.secondary))
                    typeface = resources.getFont(R.font.montserrat_bold)
                }
                deckCard.visibility = View.GONE
            } else {
                dateTime.apply {
                    text = context.getString(
                        R.string.starting_in,
                        item.startAt.asLocalDateTime.asEstimateTime
                    )
                    setTextColor(context.getColor(R.color.secondary))
                }
            }
            deckCard.setOnClickListener {
                if(Utils.copyTextToClipboard(requireContext(), item.id)) {
                    showInfo(getString(R.string.exam_code_copied_to_clipboard))
                }
            }
            if(item.isOwner) {
                teacherLabel.visibility = View.VISIBLE
            } else {
                studentLabel.visibility = View.VISIBLE
            }
            root.apply {
                (layoutParams as MarginLayoutParams).let {
                    it.bottomMargin = 35
                    root.layoutParams = it
                }
                setOnClickListener {
                    if(item.isOwner) {
                        openDetailExamForTeacher(item)
                    } else {
                        if(item.isAnswered) {
                            openDetailExamForStudent(item)
                        } else {
                            openExam(item)
                        }
                    }
                }
            }
        }
    }

    private val detailExamViewModel: DetailExamViewModel by activityViewModels()
    private fun openDetailExamForStudent(exam: Exam) {
        findNavController().navigate(R.id.action_nav_home_to_nav_student_result, Bundle().apply {
            putString(EXAM_ID, exam.id)
        })
    }
    private fun openDetailExamForTeacher(exam: Exam) {
        detailExamViewModel.setExam(exam).also {
            findNavController().navigate(R.id.action_nav_home_to_nav_exam_detail)
        }
    }
    private fun openExam(exam: Exam) {
        if(!exam.isOngoing) {
            showInfo(getString(R.string.this_exam_has_not_started_yet))
            return
        }
        startActivity(Intent(requireActivity(), ExamActivity::class.java).apply {
            putExtra("exam-id", exam.id)
        })
    }

    private val appbarBinding: JoinExamFormAppbarLayout by lazy {
        JoinExamFormAppbarLayout(requireContext()).apply {
            textInputLayout.apply {
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        joinExam(textInputLayout.text)
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
    override fun onAppbarContentView(): View {
        return appbarBinding
    }

    private fun joinExam(code: String) {
        hideKeyboard(requireActivity())
        setLoading(true)
        Client<ExamsEndpoints, Response>(requireActivity(), ExamsEndpoints::class.java)
            .onError {
                lifecycleScope.launch {
                    showInfo(it.message)
                    delay(400)
                    setLoading(false)
                }
            }
            .onSuccess {
                showInfo(getString(R.string.successful))
                appbarBinding.textInputLayout.text = ""
                lifecycleScope.launch {
                    delay(400)
                    setLoading(false)
                    fetchUpcomingExams()
                }
            }
            .fetch { it.joinExam(code) }
    }
    private fun fetchUpcomingExams() {
        viewModel.fetchOnGoing(requireActivity()) {
            lifecycleScope.launch {
                delay(600)
                setLoading(false)
                if(it.code == 404) {
                    ongoingListAdapter.submitList(listOf())
                } else {
                    InfoDialog(requireActivity())
                        .setTitle(getString(R.string.something_went_wrong))
                        .setMessage(it.message)
                        .show()
                }
            }
        }
        viewModel.withExam(requireActivity())
            .onError {
                lifecycleScope.launch {
                    delay(600)
                    setLoading(false)
                    if(it.code == 404) {
                        upcomingListAdapter.submitList(listOf())
                        binding.upcomingEmptyState.visibility = View.VISIBLE
                    } else {
                        InfoDialog(requireActivity())
                            .setTitle(getString(R.string.something_went_wrong))
                            .setMessage(it.message)
                            .show()
                    }
                }
            }
            .fetch { it.getUpcomingExam() }
    }
    private fun showInfo(message: String) {
        InfoDialog(requireActivity()).setMessage(message).show()
    }

    override fun onResume() {
        super.onResume()
        fetchUpcomingExams()
    }
}