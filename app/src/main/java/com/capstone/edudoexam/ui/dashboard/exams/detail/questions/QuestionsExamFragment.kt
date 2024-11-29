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
import com.capstone.edudoexam.components.DialogBottom
import com.capstone.edudoexam.components.FloatingMenu
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.QuestionDiffCallback
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getColor
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
                findNavController().navigate(R.id.action_nav_exam_detail_to_nav_form_question)
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
                    setMargins(0, 0, 0, 18.dp)
                }

                actionButton.setOnClickListener { showItemMenu(item, actionButton) }

            }
        }
    }

    private fun showItemMenu(item: Question, anchor: View) {

        anchor.animate()
            .setDuration(150)
            .rotation(180f)
            .start()

        FloatingMenu(requireContext(), anchor).apply {
            val floatingMenu = this
            onDismissCallback = {
                anchor.animate()
                    .setDuration(150)
                    .rotation(0f)
                    .start()
            }
            xOffset = -300
            yOffset = 80

            addItem("Edit").apply {
                color = getColor(requireContext(), R.color.primary_light)
                icon = ContextCompat.getDrawable(context, R.drawable.baseline_edit_24)
                setOnClickListener {
                    floatingMenu.hide()
                    actionEditHandler(item)
                }
            }
            addItem("Remove").apply {
                color = getColor(requireContext(), R.color.danger)
                icon = ContextCompat.getDrawable(context, R.drawable.baseline_delete_24)
                setOnClickListener {
                    floatingMenu.hide()
                    actionRemoveHandler(item)
                }
            }
        }.show()
    }

    private fun actionEditHandler(item: Question) {
        findNavController().navigate(R.id.action_nav_exam_detail_to_nav_form_question, Bundle().apply {
            putParcelable(FormQuestionFragment.ARGS_QUESTION, item)
        })
    }

    private fun actionRemoveHandler(item: Question) {
        DialogBottom.Builder(requireActivity()).apply {
            color = getColor(requireContext(), R.color.danger)
            title = "Are you sure?"
            message = "Are you sure you want to remove question ?\nDetail as below:\nID\t: ${item.id}\nQuestion\t: ${item.description}\nThis action cannot be undone."
            acceptText = "Remove"
            acceptHandler = {

                true
            }
        }.show()
    }
}
