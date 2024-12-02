package com.capstone.edudoexam.ui.dashboard.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.FragmentHomeBinding
import com.capstone.edudoexam.databinding.ViewItemExamBinding
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamFragment
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment :
    BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::class.java),
    GenericListAdapter.ItemBindListener<Exam, ViewItemExamBinding> {

    private val listAdapter: GenericListAdapter<Exam, ViewItemExamBinding> by lazy {
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

        viewModel.exams.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                delay(600)
                listAdapter.submitList(it)
                setLoading(false)
            }
        }

        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        lifecycleScope.launch {
            delay(600)
            getParentActivity().apply {
                showNavBottom()
                addMenu(R.drawable.man) {
                    findNavController().navigate(R.id.action_nav_home_to_nav_profile)
                }
            }
            fetchUpcomingExams()
        }
    }

    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {

        binding.apply {
            codeTextView.text = item.id
            titleView.text    = item.title
            subtitleView.text = item.subTitle
            dateTime.text = item.simplifiedStartDate
            codeCopyButton.setOnClickListener {
                showToast("Code Copied")
            }
            root.apply {

                (layoutParams as MarginLayoutParams).let {
                    it.bottomMargin = 35
                    layoutParams = it
                }

                setOnClickListener {
                    if(item.isOwner) {
                        findNavController().navigate(R.id.action_nav_home_to_nav_exam_detail, Bundle().apply {
                            putString(DetailExamFragment.ARG_EXAM_ID, item.id)
                        })
                    }
                }
            }
        }
    }

    override fun onAppbarContentView(): View {
        return JoinExamFormAppbarLayout(requireContext())
    }

    private fun fetchUpcomingExams() {
        viewModel.withExam(requireActivity())
            .onError {
                lifecycleScope.launch {
                    delay(600)
                    setLoading(false)
                    InfoDialog(requireActivity())
                        .setTitle("Something went wrong")
                        .setMessage(it.message)
                        .show()
                }
            }
            .fetch { it.getUpcomingExam() }
    }

}