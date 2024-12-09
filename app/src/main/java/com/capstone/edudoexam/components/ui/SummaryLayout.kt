package com.capstone.edudoexam.components.ui

import android.R.attr
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp


class SummaryLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): LinearLayout(context, attrs, defStyleAttr) {

    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        orientation = VERTICAL
    }

    fun addItem(@DrawableRes icon: Int, score: Int) {
        val imageView = createImageView(icon)
        val progressBar = createProgressBar()
        val textView = createTextView()

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 14.dp)
            }
            addView(imageView)
            addView(progressBar)
            addView(textView)
        }

        post {
            animateTextView(textView, 0, score, 600)
            val animator = ValueAnimator.ofInt(0, score * 100)
            animator.duration = 1000
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation ->
                val progressValue = animation.animatedValue as Int
                progressBar.progress = progressValue

                val normalizedAlpha = progressValue / 2000f
                progressBar.alpha = normalizedAlpha.coerceIn(0f, 1f)
            }
            animator.start()
        }

        addView(row)
    }

    private fun createImageView(@DrawableRes icon: Int): ImageView {
        return ImageView(context).apply {
            setImageResource(icon)
            layoutParams = LayoutParams(25.dp, 25.dp).apply {
                marginEnd = 8.dp
            }
        }
    }

    private fun createProgressBar(): ProgressBar {
        return ProgressBar(context, null, attr.progressBarStyleHorizontal).apply {
            progressDrawable = ContextCompat.getDrawable(context, R.drawable.progressbar_bg)
            isIndeterminate = false
            max = 100 * 100
            progress = 0
            layoutParams = LayoutParams(0, 20.dp, 1f).apply {
                marginStart = 5.dp
                marginEnd = 8.dp
            }
        }
    }

    private fun createTextView(): TextView {
        return TextView(context).apply {
            text = "0%"
            setTypeface(resources.getFont(R.font.montserrat_bold))
            layoutParams = LayoutParams(32.dp, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
        }
    }

    private fun animateTextView(textView: TextView, startValue: Int, endValue: Int, duration: Long) {
        val valueAnimator = ValueAnimator.ofInt(startValue, endValue)
        valueAnimator.duration = duration
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()

        valueAnimator.addUpdateListener { animator ->
            textView.text = "${animator.animatedValue}%"
        }

        valueAnimator.start()
    }


}