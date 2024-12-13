package com.il4mb.edudoexam.ui.dashboard.histories.teacher

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.LinearLayout.LayoutParams
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.ui.QuestionNode
import com.il4mb.edudoexam.components.ui.QuestionsNodeLayout
import com.il4mb.edudoexam.components.ui.ResultCard
import com.il4mb.edudoexam.components.ui.SummaryLayout
import com.il4mb.edudoexam.databinding.FragmentExamTeacherResultBinding
import com.il4mb.edudoexam.models.AnswerContainer
import com.il4mb.edudoexam.models.Question
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExamTeacherResultFragment :
    BaseFragment<FragmentExamTeacherResultBinding>(FragmentExamTeacherResultBinding::class.java),
    QuestionsNodeLayout.ItemNodeListener, AdapterView.OnItemSelectedListener {

    private val emotionIcons = mapOf(
        "Anger" to R.drawable.emo_angry,
        "Fear" to R.drawable.emo_fear,
        "Surprised" to R.drawable.emo_shocked,
        "Happy" to R.drawable.emo_smiling,
        "Sad" to R.drawable.emo_sad,
        "Disgust" to R.drawable.emo_tired,
        "Neutral" to R.drawable.emo_neutral
    )
    private val liveData: ExamTeacherResultViewModel by viewModels()
    private var examId: String = ""
    private var questions: MutableList<Question> = mutableListOf()
    private var answers: AnswerContainer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            // examId = getString(ExamStudentResultFragment.EXAM_ID, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liveCycleObserve()
        setupUI()
        lifecycleScope.launch {
            delay(400)
            fetchResultExam()
        }
    }

    private fun fetchResultExam() {
        liveData.fetchData(requireActivity(), examId) {
            showErrorMessage("Caught an Error", it.message, "Retry") { fetchResultExam() }
        }
    }

    private fun setupUI() {
        binding.apply {
            animateTextView(correctAnswerCard, 0, 85, 600)
            animateTextView(wrongAnswerCard, 0, 15, 600)
            animateTextView(questionLengthCard, 0, 45, 600)
            questionsNodeLayout.apply {
                itemNodeListener = this@ExamTeacherResultFragment
                nodeLength = 15
            }
            usersSpinner.onItemSelectedListener = this@ExamTeacherResultFragment
        }
    }

    private fun liveCycleObserve() {
        liveData.result.observe(viewLifecycleOwner) {
            binding.apply {
                titleView.text = it.title
                subtitleView.text = it.subTitle
                if(it.users.isEmpty()) {
                    participantsEmptyState.visibility = View.VISIBLE
                    contentContainer.visibility = View.GONE
                } else {
                    participantsEmptyState.visibility = View.GONE
                    if(it.questions.isEmpty()) {
                        questionsEmptyState.visibility = View.VISIBLE
                        contentContainer.visibility = View.GONE
                    } else {
                        questionsEmptyState.visibility = View.GONE
                        contentContainer.visibility = View.VISIBLE
                    }
                }
                usersSpinner.apply {
                    adapter = UserSpinnerAdapter(requireContext(), it.users)
                }
            }
        }
    }

    private fun updateUI(answerContainer: AnswerContainer?, questions: MutableList<Question>) {
        val questionsLength = questions.size
        val wrongLength   = answerContainer?.getTotalWrong(questions) ?: questionsLength
        val correctLength = answerContainer?.getTotalCorrect(questions) ?: 0
        binding.apply {
            animateTextView(questionLengthCard, 0, questionsLength, 600)
            animateTextView(correctAnswerCard, 0, correctLength, 600)
            animateTextView(wrongAnswerCard, 0, wrongLength, 600)
            questionsNodeLayout.nodeLength = questionsLength
        }
        updateSummaryUI(answerContainer?.summaryAccumulation())
    }

    private fun updateSummaryUI(summaries: MutableMap<String, Int>?) {
        summaries?.let { mutableMap ->
            val maxValue = mutableMap.values.sum()
            binding.apply {
                summaryLayout.removeAllViews()
                mutableMap.forEach { (key, value) ->
                    val icon = emotionIcons[key]
                    val percentage = if (maxValue > 0) (value * 100) / maxValue else 0
                    if (icon != null) {
                        summaryLayout.addItem(icon, percentage)
                    }
                }
            }
        } ?: run {
            binding.summaryLayout.removeAllViews()
        }
    }

    private fun animateTextView(textView: ResultCard, startValue: Int, endValue: Int, duration: Long) {
        val valueAnimator = ValueAnimator.ofInt(startValue, endValue)
        valueAnimator.duration = duration
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()

        valueAnimator.addUpdateListener { animator ->
            textView.score = animator.animatedValue.toString()
        }

        valueAnimator.start()
    }

    private fun createSummaryInfoWindow(summaries: MutableMap<String, Int>): View {
        val maxValue = summaries.values.sum()

        return SummaryLayout(requireContext()).apply {
            summaries.forEach { (key, value) ->
                val icon = emotionIcons[key]
                val percentage = if (maxValue > 0) (value * 100) / maxValue else 0
                if (icon != null) {
                    addItem(icon, percentage)
                }
                layoutParams = MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 14.dp, 0, 0)
                }
            }
        }
    }

    private var isDialogVisible = false
    private fun showErrorMessage(title: String, message: String, actionText: String, actionCallback: () -> Unit) {
        if (isDialogVisible) return
        isDialogVisible = true
        DialogBottom.dismissAll(requireActivity())

        DialogBottom
            .Builder(requireActivity())
            .apply {
                this.title = title
                this.message = message
                acceptText = actionText
                acceptHandler = {
                    isDialogVisible = false
                    actionCallback()
                    true
                }
            }
            .show().apply {
                dismissible = false
            }
            .onDismissCallback {
                isDialogVisible = false
            }
    }
    companion object {
        const val EXAM_ID = "exam-id"
    }

    override fun onNodeCreate(node: QuestionNode, index: Int) {
        questions.getOrNull(index)?.let { question ->
            val answer = answers?.data?.find { it.questionId == question.id }
            if (answer?.choice == question.correctOption) {
                node.mainColor = requireContext().getColor(R.color.primary_light)
            } else {
                node.mainColor = Color.parseColor("#FF0000")
            }
            node.questionInfoWindow.apply {

                title = "Summary of Question ${question.order}"
                answer?.let {
                    message = HtmlCompat
                        .fromHtml(
                            "Your Answer is <b>${it.choice}</b>",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    addView(createSummaryInfoWindow(it.summaryMap))
                } ?: run {
                    message = "Answer is missing!"
                }
            }
        }
    }
    override fun onItemClick(node: QuestionNode, index: Int) {
        node.showInfoWindow()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        (parent?.adapter as? UserSpinnerAdapter)?.getItem(position)?.let { user->
            val answersWrapper: MutableList<AnswerContainer> = liveData.result.value?.answers?: mutableListOf()
            questions = liveData.result.value?.questions?: mutableListOf()
            answersWrapper.find { it.userId ==  user.id}?.let {
                answers = it
            }
            updateUI(answers, questions)
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        (parent?.adapter as? UserSpinnerAdapter)?.getItem(0)?.let { user->
            val answers: MutableList<AnswerContainer> = liveData.result.value?.answers?: mutableListOf()
            val questions: MutableList<Question> = liveData.result.value?.questions?: mutableListOf()
            answers.find { it.userId ==  user.id}?.let {
                updateUI(it, questions)
            }
        }
    }

}