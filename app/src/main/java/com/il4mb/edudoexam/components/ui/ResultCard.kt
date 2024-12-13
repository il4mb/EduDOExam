package com.il4mb.edudoexam.components.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.il4mb.edudoexam.R
import com.google.android.material.card.MaterialCardView

class ResultCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr){

    private val linearLayout: LinearLayout = LinearLayout(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        ).apply {
           setPadding(0,10.dp, 0, 0)
        }
        orientation = LinearLayout.VERTICAL
    }

    private val scoreTextView: TextView
    private val labelTextView: TextView

    private var _score: String = "0"
    var score: String
        get() = _score
        set(value) {
            _score = value
            scoreTextView.text = value
        }

    private var _label: String = ""
    var label: String
        get() = _label
        set(value) {
            _label = value
            labelTextView.text = value
        }

    init {
        // Initialize TextViews
        scoreTextView = TextView(context).apply {
            text = _score
            gravity = Gravity.CENTER
            textSize = 34f
            typeface = resources.getFont(R.font.montserrat_bold)
        }
        labelTextView = TextView(context).apply {
            text = _label
            gravity = Gravity.END or Gravity.BOTTOM
            textSize = 11f
            typeface = resources.getFont(R.font.montserrat)
        }

        // Load attributes
        context.withStyledAttributes(attrs, R.styleable.ResultCard) {
            score = getInt(R.styleable.ResultCard_score, 0).toString()
            label = getString(R.styleable.ResultCard_label) ?: ""
        }

        // Add views
        addView(linearLayout)
        linearLayout.addView(scoreTextView)
        linearLayout.addView(labelTextView)

        // Set layout params
        labelTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END
            setMargins(10.dp, 0, 10.dp, 10.dp)
        }

        scoreTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ).apply {
            gravity = Gravity.CENTER
        }
    }

    private val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()
}
