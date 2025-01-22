package com.il4mb.edudoexam.components.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.tools.Utils.Companion.dp

class StepsIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var steps: Int = 3
        set(value) {
            field = value
            resetUI()
        }
    var currentStep = 0
        set(value) {
            field = value
            updateUI()
        }
    private var _background: Int = 0
        set(value) {
            field = value
            updateUI()
        }


    init {
        orientation = HORIZONTAL
        resetUI()

        context.withStyledAttributes(attrs, R.styleable.StepsIndicator) {
            getInt(R.styleable.StepsIndicator_step, 3).let {
                steps = it
            }
            getInt(R.styleable.StepsIndicator_currentStep, 0).let {
                currentStep = it
            }
        }
    }

    private fun updateUI() {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            view.alpha = if (i == currentStep) {
                view.backgroundTintList = ColorStateList.valueOf(_background)
                1f
            } else {
                view.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.black))
                0.3f
            }
        }
    }

    private fun resetUI() {
        removeAllViews()
        for (i in 0 until steps) {
            addView(
                View(context).apply {
                    background = ContextCompat.getDrawable(context, R.drawable.step_indicator)
                    layoutParams = LayoutParams(15.dp, 15.dp).apply {
                        if (i != steps - 1) {
                            rightMargin = 8.dp
                        }
                    }
                    alpha = 0.3f
                }
            )
        }
        updateUI()
    }

    override fun setBackground(background: Drawable?) {
        _background = (background as ColorDrawable).color
    }

    override fun setBackgroundColor(color: Int) {
        _background = color
    }
}
