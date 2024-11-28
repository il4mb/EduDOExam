package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.FloatingMenu
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.QuestionDiffCallback
import com.capstone.edudoexam.components.Utils.Companion.dp
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
            ViewItemQuestionBinding::class.java,
             this,
            QuestionDiffCallback()
        )
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            viewModel.moveItem(fromPosition, toPosition)

            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            recyclerView.adapter?.notifyItemChanged(fromPosition, false)
            recyclerView.adapter?.notifyItemChanged(toPosition, false)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            genericAdapter.submitList(questions)
        }

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

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewBind(binding: ViewItemQuestionBinding, item: Question, position: Int) {
        Log.d("QuestionsExamFragment", "onViewBind: ${item.order}")
        binding.apply {
            questionDescription.text = item.description
            orderNumber.text = "${item.order}."

            root.apply {
                layoutParams = (root.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins(0, 0, 0, 8.dp)
                }
                setOnClickListener {
                    FloatingMenu(requireContext(), it).apply {
                        xOffset = it.width / 2
                        yOffset = it.height / 2

                        addItem("Edit").apply {
                            icon = ContextCompat.getDrawable(context, R.drawable.baseline_edit_24)
                        }
                        addItem("Remove").apply {
                            icon = ContextCompat.getDrawable(context, R.drawable.baseline_delete_24)
                            setOnClickListener {  }
                        }
                    }
                        .show()
                }
            }
        }
    }
}
