package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.QuestionDiffCallback
import com.capstone.edudoexam.databinding.FragmentQuestionsExamBinding
import com.capstone.edudoexam.databinding.ViewItemQuestionBinding
import com.capstone.edudoexam.models.Question

class QuestionsExamFragment : Fragment(),
    GenericListAdapter.ItemBindListener<Question, ViewItemQuestionBinding> {

    private val binding: FragmentQuestionsExamBinding by lazy {
        FragmentQuestionsExamBinding.inflate(layoutInflater)
    }

    private val viewModel: QuestionsExamViewModel by viewModels()

    private val genericAdapter: GenericListAdapter<Question, ViewItemQuestionBinding> by lazy {
        GenericListAdapter(
            inflateBinding = ViewItemQuestionBinding::inflate,
            onItemBindCallback = this,
            diffCallback = QuestionDiffCallback()
        )
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val itemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            // Notify ViewModel to update data
            viewModel.moveItem(fromPosition, toPosition)

            // Notify adapter about the move
            genericAdapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // No action needed for swipe
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe questions and update the adapter
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            genericAdapter.submitList(questions)
        }

        // Initialize RecyclerView with ItemTouchHelper
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericAdapter
                itemTouchHelper = ItemTouchHelper(itemTouchCallback).also { it.attachToRecyclerView(this) }
            }

            floatingActionButton.setOnClickListener {
                findNavController().navigate(R.id.nav_form_question)
            }
        }

        // Add sample data to the ViewModel
        for (i in 1 until 5) {
            viewModel.addQuestion(
                Question(
                    "$i",
                    "John Doe $i",
                    "This is a question $i",
                    10,
                    'A',
                    i,
                    mapOf(
                        'A' to "Option $i.A",
                        'B' to "Option $i.B",
                        'C' to "Option $i.C",
                        'D' to "Option $i.D"
                    )
                )
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewBind(binding: ViewItemQuestionBinding, item: Question) {
        binding.apply {
            questionDescription.text = item.description

            // Handle drag gesture
            orderHandle.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    // Start drag
                    // itemTouchHelper.startDrag(binding)
                }
                false
            }
        }
    }
}
