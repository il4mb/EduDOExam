package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.payloads.QuestionsOrderPayload
import com.capstone.edudoexam.api.response.ResponseError
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getColor
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.components.ui.FloatingMenu
import com.capstone.edudoexam.databinding.FragmentExamDetailQuestionsBinding
import com.capstone.edudoexam.databinding.ViewItemQuestionBinding
import com.capstone.edudoexam.databinding.ViewModalQuestionBinding
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.models.QuestionOptions
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import com.capstone.edudoexam.ui.dashboard.exams.detail.questions.FormQuestionFragment.Companion.asQuestionOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuestionsExamFragment : Fragment(),
    GenericListAdapter.ItemBindListener<Question, ViewItemQuestionBinding> {

    private val binding: FragmentExamDetailQuestionsBinding by lazy {
        FragmentExamDetailQuestionsBinding.inflate(layoutInflater)
    }
    private var examId: String? = null
    private val viewModel: DetailExamViewModel by activityViewModels()
    private val genericAdapter: QuestionListAdapter by lazy {
        QuestionListAdapter(this).apply {
            lifecycleScope.launch {
                delay(600)
                setOnItemMovedListener { doSaveMovedQuestion() }
            }
        }
    }
    private val itemTouchHelper = ItemTouchHelper(genericAdapter.itemTouchCallback)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveCycleObserve()
        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericAdapter
                itemTouchHelper.attachToRecyclerView(this)
            }

            floatingActionButton.setOnClickListener {
                findNavController().navigate(R.id.action_nav_exam_detail_to_nav_form_question, Bundle().apply {
                    putString(FormQuestionFragment.ARGS_QUESTION_EXAM_ID, examId)
                })
            }
        }
    }

    private fun liveCycleObserve() {
        viewModel.apply {
            questions.observe(viewLifecycleOwner) { questions ->
                lifecycleScope.launch {
                    if(questions != null && questions.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        genericAdapter.submitList(listOf())
                    } else {
                        binding.emptyState.visibility = View.GONE
                        genericAdapter.submitList(questions)
                    }
                    delay(400)
                    setLoading(false)
                }
            }
            exam.observe(viewLifecycleOwner) {
                examId = it.id
                if(it.isOngoing) {
                    binding.floatingActionButton.visibility = View.GONE
                    itemTouchHelper.attachToRecyclerView(null)
                }
                doFetchQuestions()
            }
        }
    }

    fun doFetchQuestions() {
        examId?.let { examId ->
            setLoading(true)
            viewModel.withQuestions(requireActivity())
                .onError {
                    if(it.code != 404) {
                        errorHandler(it)
                    } else {
                        setLoading(false)
                        binding.emptyState.visibility = View.VISIBLE
                        genericAdapter.submitList(listOf())
                    }
                }
                .fetch { it.getQuestions(examId) }
        } ?: {
            InfoDialog(requireActivity())
                .setMessage("Missing exam id")
                .show()
        }
    }

    private fun doRemoveQuestion(item: Question) {
        examId?.let { examId ->
            setLoading(true)
            viewModel.withNoResult(requireActivity())
                .onSuccess {
                    lifecycleScope.launch {
                        delay(400)
                        setLoading(false)
                        doFetchQuestions()
                    }
                }
                .onError { errorHandler(it) }
                .fetch { it.removeQuestion(examId = examId, questionId = item.id) }
        } ?: {
            InfoDialog(requireActivity())
                .setMessage(getString(R.string.missing_exam_id))
                .show()
        }
    }

    private fun doSaveMovedQuestion() {
        val questions = genericAdapter.currentList.toMutableList()
        examId?.let { examId ->
            val map = questions.mapIndexed { _, question -> question.id to question.order }.toMap()
            viewModel.withNoResult(requireActivity())
                .onError { errorHandler(it) }
                .fetch { it.saveQuestionOrder(examId = examId, body = QuestionsOrderPayload(map)) }
        }
    }

    private fun showSwapModal(item: Question) {
        val questions = viewModel.questions.value ?: return
        val orders = questions.map { it.order }
        val currentOrder = item.order

        val spinnerAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, orders.map { it.toString() }) {
            override fun isEnabled(position: Int): Boolean {
                return orders[position] != currentOrder
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                if (!isEnabled(position)) {
                    view.alpha = 0.5f
                } else {
                    view.alpha = 1f
                }
                (view as TextView).apply {
                    typeface = resources.getFont(R.font.montserrat_semi_bold)
                    textSize = 16f
                }

                return view
            }
        }

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = Spinner(requireContext()).apply {
            adapter = spinnerAdapter
            setSelection(orders.indexOf(currentOrder))
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_frame_outline)
            setPadding(12.dp, 10.dp, 10.dp, 12.dp)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 12.dp, 0, 18.dp)
            }
        }

        val dialog = DialogBottom.Builder(requireActivity()).apply {
            title = getString(R.string.choose_position)
            view  = spinner
            acceptText = "Swap"
        }.show()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOrder = parent?.getItemAtPosition(position) as? String
                if (selectedOrder != null) {
                    val targetOrder = selectedOrder.toInt()
                    if (targetOrder != item.order) {
                        genericAdapter.moveItem(item.order, targetOrder)
                        dialog.dismiss()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun errorHandler(error: ResponseError) {
        lifecycleScope.launch {
            delay(400)
            setLoading(false)
            Snackbar.with(binding.root).show(getString(R.string.something_went_wrong), error.message, Snackbar.LENGTH_LONG)
        }
    }

    private fun navigateToEdit(item: Question) {
        findNavController().navigate(R.id.action_nav_exam_detail_to_nav_form_question, Bundle().apply {
            putString(FormQuestionFragment.ARGS_QUESTION_ID, item.id)
            putString(FormQuestionFragment.ARGS_QUESTION_EXAM_ID, examId)
        })
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

            addItem(getString(R.string.swap)).apply {
                color = getColor(requireContext(), R.color.primary_light)
                icon = ContextCompat.getDrawable(context, R.drawable.baseline_multiple_stop_24)
                setOnClickListener {
                    floatingMenu.hide()
                    showSwapModal(item)
                }
            }

            addItem(getString(R.string.edit)).apply {
                color = getColor(requireContext(), R.color.primary_light)
                icon = ContextCompat.getDrawable(context, R.drawable.baseline_edit_24)
                setOnClickListener {
                    floatingMenu.hide()
                    navigateToEdit(item)
                }
            }
            addItem(getString(R.string.remove)).apply {
                color = getColor(requireContext(), R.color.danger)
                icon = ContextCompat.getDrawable(context, R.drawable.baseline_delete_24)
                setOnClickListener {
                    floatingMenu.hide()
                    actionRemoveHandler(item)
                }
            }
        }.show()
    }

    private fun actionRemoveHandler(item: Question) {
        DialogBottom.Builder(requireActivity()).apply {
            color = getColor(requireContext(), R.color.danger)
            title = getString(R.string.are_you_sure)
            message = getString(
                R.string.are_you_sure_you_want_to_remove_question_detail_as_below_id_question_this_action_cannot_be_undone,
                item.id,
                item.description
            )
            acceptText = getString(R.string.remove)
            acceptHandler = {
                doRemoveQuestion(item)
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
                    setMargins(0, 12.dp, 0, 18.dp)
                }
                actionButton.setOnClickListener { showItemMenu(item, actionButton) }
                if(viewModel.exam.value?.isOngoing == true) {
                    actionButton.visibility = View.GONE
                    setOnClickListener { showQuestionDialog(item) }
                }
            }
        }
    }

    private val modalQuestionBinding: ViewModalQuestionBinding by lazy {
        ViewModalQuestionBinding.inflate(layoutInflater)
    }

    @SuppressLint("StringFormatMatches")
    private fun showQuestionDialog(question: Question) {
        modalQuestionBinding.apply {
            inputDuration.text = getString(R.string.minute, question.duration)
            inputDescription.editText!!.setText(question.description)
            if(question.image != null) {
                imageCard.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(question.image)
                    .into(imageCard[0] as ImageView)
            }
            optionsLayout.apply {
                isEnabled = false
                options = question.options.asQuestionOptions
            }
        }
        DialogBottom.Builder(requireActivity()).apply {
            title = getString(R.string.question_detail)
            view  = modalQuestionBinding.root
            isAcceptActionButtonVisible = false
            isCancelActionButtonVisible = false
        }.show()
    }

    companion object {
        fun newInstance(): QuestionsExamFragment {
            val fragment = QuestionsExamFragment()
            return fragment
        }
    }

}
