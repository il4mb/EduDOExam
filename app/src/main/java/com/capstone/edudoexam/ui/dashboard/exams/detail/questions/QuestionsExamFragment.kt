package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.ui.FloatingMenu
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.QuestionDiffCallback
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getColor
import com.capstone.edudoexam.databinding.FragmentQuestionsExamBinding
import com.capstone.edudoexam.databinding.ViewItemQuestionBinding
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuestionsExamFragment : Fragment(),
    GenericListAdapter.ItemBindListener<Question, ViewItemQuestionBinding> {

    private val binding: FragmentQuestionsExamBinding by lazy {
        FragmentQuestionsExamBinding.inflate(layoutInflater)
    }
    private val viewModel: DetailExamViewModel by activityViewModels()
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

            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            recyclerView.adapter?.notifyItemChanged(fromPosition, false)
            recyclerView.adapter?.notifyItemChanged(toPosition, false)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }
    }
    private var examId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        examId = arguments?.getString(ARG_EXAM_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            lifecycleScope.launch {
                delay(400)
                setLoading(false)
                genericAdapter.submitList(questions)
            }
        }

        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericAdapter
                itemTouchHelper = ItemTouchHelper(itemTouchCallback).also { it.attachToRecyclerView(this) }
            }

            floatingActionButton.setOnClickListener {
                findNavController().navigate(R.id.action_nav_exam_detail_to_nav_form_question, Bundle().apply {
                    putString(FormQuestionFragment.ARGS_QUESTION_EXAM_ID, examId)
                })
            }
        }

        lifecycleScope.launch {
            doFetchQuestions()
        }
    }

    fun doFetchQuestions() {
        setLoading(true)
        examId?.let { examId ->
            viewModel.withQuestions(requireActivity())
                .onError {
                    lifecycleScope.launch {
                        delay(400)
                        setLoading(false)
                        Snackbar.with(binding.root).show("Something went wrong", it.message, Snackbar.LENGTH_LONG)
                    }
                }
                .fetch { it.getQuestions(examId) }
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
            putString(FormQuestionFragment.ARGS_QUESTION_ID, item.id)
            putString(FormQuestionFragment.ARGS_QUESTION_EXAM_ID, examId)
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

    private fun setLoading(isLoading: Boolean) {
        (requireActivity() is DashboardActivity).apply {
            (requireActivity() as DashboardActivity).setLoading(isLoading)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewBind(binding: ViewItemQuestionBinding, item: Question, position: Int) {

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


    companion object {
        private const val ARG_EXAM_ID = "exam_id"

        fun newInstance(examId: String?): QuestionsExamFragment {
            val fragment = QuestionsExamFragment()
            val args = Bundle()
            args.putString(ARG_EXAM_ID, examId)
            fragment.arguments = args
            return fragment
        }
    }

}
