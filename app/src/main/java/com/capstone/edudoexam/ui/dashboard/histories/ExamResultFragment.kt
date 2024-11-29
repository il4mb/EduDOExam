package com.capstone.edudoexam.ui.dashboard.histories

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import com.capstone.edudoexam.components.AppFragment
import com.capstone.edudoexam.components.QuestionNode
import com.capstone.edudoexam.components.QuestionsNodeLayout
import com.capstone.edudoexam.components.ResultCard
import com.capstone.edudoexam.databinding.FragmentExamResultBinding
import com.capstone.edudoexam.models.ExamResult

class ExamResultFragment :
    AppFragment<FragmentExamResultBinding>(FragmentExamResultBinding::class.java),
    QuestionsNodeLayout.ItemNodeListener {

    private lateinit var history: ExamResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        history = arguments?.getParcelable(ARGS_ID)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            titleView.text = history.title
            subtitleView.text = history.subTitle
            dateTimeView.text = history.startDate

            animateTextView(correctAnswerCard, 0, 85, 600)
            animateTextView(wrongAnswerCard, 0, 15, 600)
            animateTextView(questionLengthCard, 0, 45, 600)

            questionsNodeLayout.apply {
                itemNodeListener = this@ExamResultFragment
                nodeLength = 100
            }
        }
    }

    private fun animateTextView(
        textView: ResultCard,
        startValue: Int,
        endValue: Int,
        duration: Long
    ) {
        val valueAnimator = ValueAnimator.ofInt(startValue, endValue)
        valueAnimator.duration = duration
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()

        valueAnimator.addUpdateListener { animator ->
            textView.score = animator.animatedValue.toString()
        }

        valueAnimator.start()
    }

    companion object {
        const val ARGS_ID = "story-id"
    }

    override fun onNodeCreate(node: QuestionNode, index: Int) {
        if((index + 1) == 1) {
            node.mainColor = Color.parseColor("#FF0000")
        }

        if((index + 1) == 5) {
            node.mainColor = Color.parseColor("#FF0000")
        }
        node.questionInfoWindow.message = "" //HtmlCompat.fromHtml("", HtmlCompat.FROM_HTML_MODE_LEGACY)


            node.questionInfoWindow.title = ""
    }

    override fun onItemClick(node: QuestionNode, index: Int) {

    }

}