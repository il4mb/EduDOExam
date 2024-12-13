package com.il4mb.edudoexam.ui.dashboard.histories.student

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.asEstimateTime
import com.il4mb.edudoexam.components.Utils.Companion.asLocalDateTime
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.ui.QuestionNode
import com.il4mb.edudoexam.components.ui.QuestionsNodeLayout
import com.il4mb.edudoexam.components.ui.ResultCard
import com.il4mb.edudoexam.components.ui.SummaryLayout
import com.il4mb.edudoexam.components.ui.UiHelper
import com.il4mb.edudoexam.databinding.FragmentExamStudentResultBinding
import com.il4mb.edudoexam.models.AnswerContainer
import com.il4mb.edudoexam.models.Question
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

    private val liveData: StudentResultViewModel by activityViewModels()
    private var questions: MutableList<Question> = mutableListOf()
    private var answer: AnswerContainer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveCycleObserver()
        binding.userCard.actionButton.visibility = View.GONE
        binding.apply {
            questionsNodeLayout.apply {
                itemNodeListener = this@ExamStudentResultFragment
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun liveCycleObserver() {
        liveData.apply {
            exam.observe(viewLifecycleOwner) {
                binding.apply {
                    dateTimeView.text = "End at ${it.finishAt.asLocalDateTime.asEstimateTime}"
                    titleView.text    = it.title
                    subtitleView.text = it.subTitle
                    it.owner?.let { owner ->
                        UiHelper.setupUserImage(requireContext(), ownerPhoto, owner)
                        ownerName.text = owner.name
                    }
                }
            }

            participant.observe(viewLifecycleOwner) {
                this@ExamStudentResultFragment.answer = it.answer
                lifecycleScope.launch {
                    delay(400)
                    updateSummaryUI()
                }
                it.user.let { user ->
                    binding.apply {
                        userCard.apply {
                            if(user.photo != null) {
                                Glide.with(requireContext())
                                    .load(user.photo)
                                    .into(userPhoto)
                            } else {
                                userPhoto.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        if (user.gender == 1) R.drawable.man else R.drawable.woman
                                    )
                                )
                            }
                            userName.text  = user.name
                            userEmail.text = user.email
                        }
                    }
                }
            }

            questions.observe(viewLifecycleOwner) {
                this@ExamStudentResultFragment.questions = it
                lifecycleScope.launch {
                    delay(400)
                    updateSummaryUI()
                }
            }

        }
    }

    private fun updateSummaryUI() {
        val questionsLength = questions.size
        val wrongLength   = answer?.getTotalWrong(questions) ?: 0
        val correctLength = answer?.getTotalCorrect(questions) ?: 0
        binding.apply {
            animateTextView(questionLengthCard, questionsLength)
            animateTextView(correctAnswerCard, correctLength)
            animateTextView(wrongAnswerCard, wrongLength)
            questionsNodeLayout.nodeLength = questionsLength
        }

        val summaries = answer?.summaryAccumulation()
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

    private fun animateTextView(textView: ResultCard, endValue: Int) {
        val valueAnimator = ValueAnimator.ofInt(0, endValue)
        valueAnimator.duration = 600L
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()

        valueAnimator.addUpdateListener { animator ->
            textView.score = animator.animatedValue.toString()
        }

        valueAnimator.start()
    }

    override fun onNodeCreate(node: QuestionNode, index: Int) {
        questions.getOrNull(index)?.let { question ->

            val answer = answer?.data?.find { it.questionId == question.id }
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

}