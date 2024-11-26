package com.capstone.edudoexam.ui.dashboard.exams

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.AppFragment
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.databinding.FragmentExamsBinding
import com.capstone.edudoexam.databinding.ViewItemExamBinding
import com.capstone.edudoexam.models.Exam

class ExamsFragment :
    AppFragment<FragmentExamsBinding, ExamsViewModel>(FragmentExamsBinding::inflate),
    GenericListAdapter.ItemBindListener<Exam, ViewItemExamBinding> {

    private var data: ArrayList<Exam> = ArrayList()
    private lateinit var listAdapter: GenericListAdapter<Exam, ViewItemExamBinding>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = GenericListAdapter(
            inflateBinding = ViewItemExamBinding::inflate,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
        for(i in 1..15) {
            data.add(
                Exam(
                    "DUMMY-$i",
                    "November 25, 2024 at 1:32:29 PM UTC+7",
                    "November 23, 2024 at 5:05:56 PM UTC+7",
                    "XT63TAP4XA",
                    "Ujian Tengah Semester ${(i+1)/10}",
                    "Kelas ${(i+1)/6}")
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.exams.observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
        }
        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        // trigger view model
        viewModel.store(data)
    }

    override fun onViewBind(binding: ViewItemExamBinding, item: Exam) {
        binding.apply {
            codeTextView.text = item.id
            titleView.text = item.title
            subtitleView.text = item.subTitle
            dateTime.text = item.startDate
            codeCopyButton.setOnClickListener {
                viewModel.store(data)
                showToast("Code Copied")
            }
            (root.layoutParams as MarginLayoutParams).let {
                it.bottomMargin = 35
                root.layoutParams = it
            }

            root.setOnClickListener {
                findNavController().navigate(R.id.nav_exam_detail)
            }
        }
    }
}