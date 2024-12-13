package com.il4mb.edudoexam.ui.dashboard.histories

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.api.response.ResponseError
import com.il4mb.edudoexam.components.ExamDiffCallback
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.components.ui.UiHelper
import com.il4mb.edudoexam.databinding.FragmentHistoriesBinding
import com.il4mb.edudoexam.databinding.ViewItemExamBinding
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.ui.dashboard.SharedViewModel
import com.il4mb.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import com.il4mb.edudoexam.ui.dashboard.histories.student.StudentResultViewModel
import kotlinx.coroutines.launch

class HistoriesFragment :
    BaseFragment<FragmentHistoriesBinding>(FragmentHistoriesBinding::class.java),
    GenericListAdapter.ItemBindListener<Exam, ViewItemExamBinding> {

    private val viewModel: HistoriesViewModel by viewModels()
    private val listAdapter: GenericListAdapter<Exam, ViewItemExamBinding> by lazy {
        GenericListAdapter(
            ViewItemExamBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBottomNavigationVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.histories.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                binding.emptyState.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.VISIBLE
            }
            listAdapter.submitList(it)
        }
        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            root.setOnRefreshListener {
                doFetchHistories()
                binding.root.isRefreshing = false
            }
        }

        doFetchHistories()
    }

    private fun doFetchHistories() {
        viewModel.withHistories(requireActivity())
            .onError { onErrorHandler(it) }
            .fetch { it.getFinished() }
    }

    private fun onErrorHandler(error: ResponseError) {
        lifecycleScope.launch {
            if(error.code == 404) {
                listAdapter.submitList(listOf())
            } else {
                InfoDialog(requireActivity())
                    .setTitle("Something went wrong")
                    .setMessage(error.message)
                    .show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {

        binding.apply {

            UiHelper.setupExamItemUI(binding, item)

            (root.layoutParams as MarginLayoutParams).let {
                it.bottomMargin = 35
                it.marginStart  = 14.dp
                it.marginEnd    = 14.dp
                root.layoutParams = it
            }

            root.setOnClickListener {
                if (item.isOwner) {
                    openDetailExamForTeacher(item)
                } else {
                    openDetailExamForStudent(item)
                }
            }
        }
    }

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val studentResultLiveData: StudentResultViewModel by activityViewModels()
    private fun openDetailExamForStudent(exam: Exam) {
        sharedViewModel.user.value?.let { user ->
            try {
                studentResultLiveData.loadData(
                    activity = requireActivity(),
                    examId   = exam.id,
                    user     = user,
                    succeed  = {
                        findNavController().navigate(R.id.action_nav_histories_to_nav_student_result)
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
    private val detailExamViewModel: DetailExamViewModel by activityViewModels()
    private fun openDetailExamForTeacher(exam: Exam) {
        detailExamViewModel.fetchExam(
            activity = requireActivity(),
            examId = exam.id,
            success = {
                try {
                    findNavController().navigate(R.id.action_nav_histories_to_nav_exam_detail)
                } catch (t:Throwable) {
                    showInfo(t.message.toString())
                    t.printStackTrace()
                }
            },
            error = {
                showInfo(it.message)
            }
        )
    }

    private fun showInfo(message: String) {
        InfoDialog(requireActivity())
            .setMessage(message)
            .show()
    }
    override fun onResume() {
        super.onResume()
        doFetchHistories()
    }
}