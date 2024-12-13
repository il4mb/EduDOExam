package com.il4mb.edudoexam.ui.dashboard.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextMenu
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ExamsEndpoints
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.components.ExamDiffCallback
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.Utils
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.Utils.Companion.hideKeyboard
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.ui.MenuLayout
import com.il4mb.edudoexam.components.ui.UiHelper
import com.il4mb.edudoexam.databinding.FragmentHomeBinding
import com.il4mb.edudoexam.databinding.ViewItemExamBinding
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.ui.dashboard.DashboardActivity
import com.il4mb.edudoexam.ui.dashboard.SharedViewModel
import com.il4mb.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import com.il4mb.edudoexam.ui.dashboard.histories.student.StudentResultViewModel
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
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val profileMenuItem: MenuLayout.MenuItem by lazy {
        MenuLayout.MenuItem(requireContext()).apply {
            setPadding(0.dp)
            setOnClickListener {
                sharedViewModel.fetchUser(requireActivity())
                findNavController().navigate(R.id.action_nav_home_to_nav_profile)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBottomNavigationVisible = true
        ViewCompat.setTransitionName(profileMenuItem[0], "user-photo")
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        liveCycleObserve()

        lifecycleScope.launch {
            delay(600)
            getParentActivity().apply {
                showNavBottom()
                addMenu(profileMenuItem)
            }
            fetchUpcomingOngoingExams()
        }
    }

    private fun setupUI() {
        binding.apply {
            root.apply {
                setOnRefreshListener {
                    fetchUpcomingOngoingExams()
                    binding.root.isRefreshing = false
                }
            }
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
        sharedViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                UiHelper.setupUserImage(requireContext(), profileMenuItem, it)
            }
        }

        viewModel.apply {
            upcomingExams.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    if(it.isNotEmpty()) {
                        binding.upcomingEmptyState.visibility = View.GONE
                    }
                    upcomingListAdapter.submitList(it)
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
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {

        binding.apply {
            UiHelper.setupExamItemUI(binding, item)
            deckCard.setOnClickListener {
                if(Utils.copyTextToClipboard(requireContext(), item.id)) {
                    showInfo(getString(R.string.exam_code_copied_to_clipboard))
                }
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
    private val studentResultLiveData: StudentResultViewModel by activityViewModels()
    private fun openDetailExamForStudent(exam: Exam) {
        sharedViewModel.user.value?.let { user ->
            try {
                studentResultLiveData.loadData(
                    activity = requireActivity(),
                    examId   = exam.id,
                    user     = user,
                    succeed  = {
                        findNavController().navigate(R.id.action_nav_home_to_nav_student_result)
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
    private fun openDetailExamForTeacher(exam: Exam) {
        detailExamViewModel.fetchExam(
            activity = requireActivity(),
            examId = exam.id,
            success = {
                try {
                    findNavController().navigate(R.id.action_nav_home_to_nav_exam_detail)
                } catch (t:Throwable) {
                    t.printStackTrace()
                }
            },
            error = { showInfo(it.message) }
        )
    }
    private fun openExam(exam: Exam) {
        if(!exam.isOngoing) {
            showInfo(getString(R.string.this_exam_has_not_started_yet))
            return
        }
        (requireActivity() as? DashboardActivity)?.startExamActivity(exam.id)
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
        Client<ExamsEndpoints, Response>(requireActivity(), ExamsEndpoints::class.java)
            .onError {
                lifecycleScope.launch {
                    showInfo(it.message)
                }
            }
            .onSuccess {
                showInfo(getString(R.string.successful))
                appbarBinding.textInputLayout.text = ""
                lifecycleScope.launch {
                    fetchUpcomingOngoingExams()
                }
            }
            .fetch { it.joinExam(code) }
    }

    private fun fetchUpcomingOngoingExams() {
        viewModel.fetchOnGoing(requireActivity()) {
            lifecycleScope.launch {
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
        fetchUpcomingOngoingExams()
    }
}