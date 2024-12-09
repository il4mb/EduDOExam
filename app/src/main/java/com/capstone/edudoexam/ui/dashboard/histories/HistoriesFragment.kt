package com.capstone.edudoexam.ui.dashboard.histories

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.response.ResponseError
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Utils.Companion.asLocalDateTime
import com.capstone.edudoexam.components.Utils.Companion.asTimeAgo
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.FragmentHistoriesBinding
import com.capstone.edudoexam.databinding.ViewItemHistoryBinding
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.ui.dashboard.histories.student.ExamStudentResultFragment
import com.capstone.edudoexam.ui.dashboard.histories.student.ExamStudentResultViewModel
import com.capstone.edudoexam.ui.dashboard.histories.teacher.ExamTeacherResultFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoriesFragment :
    BaseFragment<FragmentHistoriesBinding>(FragmentHistoriesBinding::class.java),
    GenericListAdapter.ItemBindListener<Exam, ViewItemHistoryBinding> {

    private val viewModel: HistoriesViewModel by viewModels()
    private lateinit var listAdapter: GenericListAdapter<Exam, ViewItemHistoryBinding>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBottomNavigationVisible = true

        listAdapter = GenericListAdapter(
            ViewItemHistoryBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.histories.observe(viewLifecycleOwner) {
            setLoading(false)
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
        }

        lifecycleScope.launch {
            delay(400)
            doFetchHistories()
        }
    }

    private fun doFetchHistories() {
        setLoading(true)
        viewModel.withHistories(requireActivity())
            .onError { onErrorHandler(it) }
            .fetch { it.getFinished() }

    }

    private fun onErrorHandler(error: ResponseError) {
        lifecycleScope.launch {
            delay(600)
            setLoading(false)
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
    override fun onViewBind(binding: ViewItemHistoryBinding, item: Exam, position: Int) {

        binding.apply {

            title.text = item.title
            subtitle.text = item.subTitle
            dateTime.apply {
                text = item.finishAt.asLocalDateTime.asTimeAgo
                typeface = resources.getFont(R.font.montserrat_semi_bold)
            }

            (root.layoutParams as MarginLayoutParams).let {
                it.bottomMargin = 35
                it.marginStart = 14.dp
                it.marginEnd = 14.dp
                root.layoutParams = it
            }

            root.setOnClickListener {
                if (item.isOwner) {
                    findNavController().navigate(R.id.action_nav_histories_to_nav_teacher_result, Bundle().apply {
                        putString(ExamTeacherResultFragment.EXAM_ID, item.id)
                    })
                } else {
                    findNavController().navigate(R.id.action_nav_histories_to_nav_student_result, Bundle().apply {
                        putString(ExamStudentResultFragment.EXAM_ID, item.id)
                    })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        doFetchHistories()
    }
}