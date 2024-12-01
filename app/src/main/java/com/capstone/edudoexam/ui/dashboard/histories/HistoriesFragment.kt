package com.capstone.edudoexam.ui.dashboard.histories

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.BaseFragment
import com.capstone.edudoexam.components.ExamResultDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.databinding.FragmentHistoriesBinding
import com.capstone.edudoexam.databinding.ViewItemHistoryBinding
import com.capstone.edudoexam.models.ExamResult

class HistoriesFragment :
    BaseFragment<FragmentHistoriesBinding>(FragmentHistoriesBinding::class.java),
    GenericListAdapter.ItemBindListener<ExamResult, ViewItemHistoryBinding>
{

    private var data: ArrayList<ExamResult> = ArrayList()
    private lateinit var listAdapter: GenericListAdapter<ExamResult, ViewItemHistoryBinding>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBottomNavigationVisible = true

        listAdapter = GenericListAdapter(
            ViewItemHistoryBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamResultDiffCallback()
        )
//        for(i in 1..30) {
//            data.add(
//                ExamResult(
//                    "DUMMY-$i",
//                    "November 25, 2024 at 1:32:29 PM UTC+7",
//                    "November 23, 2024 at 5:05:56 PM UTC+7",
//                    "XT63TAP4XA",
//                    "Ujian Tengah Semester ${(i+1)/10}",
//                    "Kelas ${(i+1)/6}",
//                    0.82f,
//                    "passed"
//                    )
//            )
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getParentActivity().showNavBottom()
        getViewModel(HistoriesViewModel::class.java).histories.observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
        }
        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        getViewModel(HistoriesViewModel::class.java).store(data)
    }


    @SuppressLint("SetTextI18n")
    override fun onViewBind(binding: ViewItemHistoryBinding, item: ExamResult, position: Int) {

        binding.apply {

//            title.text = item.title
//            subtitle.text = item.subTitle
//            dateTime.text = item.startDate.toString()
            score.text = "${(item.score * 100).toInt()}%"
            status.text = item.status

            (root.layoutParams as MarginLayoutParams).let {
                it.bottomMargin = 35
                root.layoutParams = it
            }

            root.setOnClickListener { navigateToHistory(item) }
        }
    }

    private fun navigateToHistory(examHistory: ExamResult) {

        findNavController().navigate(R.id.action_nav_histories_to_nav_history, Bundle().apply {
            putParcelable(ExamResultFragment.ARGS_ID, examHistory)
        })
    }
}