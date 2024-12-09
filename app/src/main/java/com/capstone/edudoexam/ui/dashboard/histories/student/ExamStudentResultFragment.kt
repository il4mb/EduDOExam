package com.capstone.edudoexam.ui.dashboard.histories.student

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.setMargins
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.asEstimateTime
import com.capstone.edudoexam.components.Utils.Companion.asLocalDateTime
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.ui.QuestionNode
import com.capstone.edudoexam.components.ui.QuestionsNodeLayout
import com.capstone.edudoexam.components.ui.ResultCard
import com.capstone.edudoexam.components.ui.SummaryLayout
import com.capstone.edudoexam.databinding.FragmentExamStudentResultBinding
import com.capstone.edudoexam.models.Answer
import com.capstone.edudoexam.models.Question
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExamStudentResultFragment :
    BaseFragment<FragmentExamStudentResultBinding>(FragmentExamStudentResultBinding::class.java),
    QuestionsNodeLayout.ItemNodeListener {

    private val emotionIcons = mapOf(
        "Anger" to R.drawable.emo_angry,
        "Fear" to R.drawable.emo_fear,
        "Surprised" to R.drawable.emo_shocked,
        "Happy" to R.drawable.emo_smiling,
        "Sad" to R.drawable.emo_sad,
        "Disgust" to R.drawable.emo_tired,
        "Neutral" to R.drawable.emo_neutral
    )
    private var examId: String = ""
    private val liveData: ExamStudentResultViewModel by viewModels()
    private var questions: MutableList<Question> = mutableListOf()
    private var answers: MutableList<Answer> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            examId = getString(EXAM_ID, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveCycleObserver()

        binding.userCard.actionButton.visibility = View.GONE

        lifecycleScope.launch {
            delay(400)
            setLoading(true)
            liveData.fetchData(requireActivity(), examId)
        }

        binding.apply {
            questionsNodeLayout.apply {
                itemNodeListener = this@ExamStudentResultFragment
            }
        }
    }

    private fun liveCycleObserver() {
        liveData.result.observe(viewLifecycleOwner) {

            it.answer?.data?.let { data ->
                answers = data
            }
            questions = it.questions

            val questionsLength = it.questions.size
            val wrongLength   = it.getTotalWrong()
            val correctLength = it.getTotalCorrect()

            binding.apply {

                dateTimeView.text = it.answer?.createdAt?.asLocalDateTime?.asEstimateTime
                titleView.text    = it.title
                subtitleView.text = it.subTitle

                animateTextView(questionLengthCard, 0, questionsLength, 600)
                animateTextView(correctAnswerCard, 0, correctLength, 600)
                animateTextView(wrongAnswerCard, 0, wrongLength, 600)
                questionsNodeLayout.nodeLength = questionsLength

                userCard.userPhoto.setImageDrawable(ContextCompat.getDrawable(requireContext(), if(it.user.gender == 1) R.drawable.man else R.drawable.woman))
                userCard.userName.text = it.user.name
                userCard.userEmail.text = it.user.email
            }

            updateSummaryUI(it.answer?.summaryAccumulation())

            lifecycleScope.launch {
                delay(600)
                setLoading(false)
            }
        }
    }

    private fun updateSummaryUI(summaries: MutableMap<String, Int>?) {
        val maxValue = summaries?.values?.sum() ?: 0
        binding.apply {
            summaryLayout.removeAllViews()
            summaries?.forEach { (key, value) ->
                val icon = emotionIcons[key]
                val percentage = if (maxValue > 0) (value * 100) / maxValue else 0
                if (icon != null) {
                    summaryLayout.addItem(icon, percentage)
                }
            }
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

    override fun onNodeCreate(node: QuestionNode, index: Int) {

        questions.getOrNull(index)?.let { question ->

            val answer = answers.find { it.questionId == question.id }
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

    override fun onItemClick(node: QuestionNode, index: Int) {
        node.showInfoWindow()
    }

    companion object {
        const val EXAM_ID = "exam-id"
    }

}