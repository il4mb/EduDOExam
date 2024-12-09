package com.capstone.edudoexam.components.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getAttr

class OptionNode  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val labelView: TextView by lazy {
        TextView(context).apply {
            background = GradientDrawable().apply {
                cornerRadius = 100f
                color = ColorStateList.valueOf(Color.TRANSPARENT)
                setStroke(1.dp, getAttr(context, android.R.attr.textColor))
            }
            typeface = resources.getFont(R.font.montserrat_bold)
            textSize = 16f
            text = "A"
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(30.dp, 30.dp)
            setTextColor(getAttr(context, android.R.attr.textColor))
        }
    }
    private val textView: TextView by lazy {
        TextView(context).apply {
            typeface = resources.getFont(R.font.montserrat)
            textSize = 14f
            text = ""
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(18.dp, 0, 0, 14.dp)
            }
            setTextColor(getAttr(context, android.R.attr.textColor))
        }
    }

    var label: Char = '\u0000'
        get() = labelView.text.elementAt(0)
        set(value) {
            field = value
            labelView.text = field.toString()
        }

    var text: String = ""
        get() = textView.text.toString()
        set(value) {
            field = value
            textView.text = field
        }

    var isActive: Boolean = false
        set(value) {
            field = value
            if(field) {
                applyActiveUI()
            } else {
                resetActiveUI()
            }
        }

    init {

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        gravity = Gravity.TOP
        addView(labelView)
        addView(textView)

        context.withStyledAttributes(attrs, R.styleable.OptionNode) {
            getString(R.styleable.OptionNode_optionText)?.let {
                text = it
            }
            getString(R.styleable.OptionNode_optionLabel)?.let {
                label = it[0]
            }
            getBoolean(R.styleable.OptionNode_optionActive, false).let {
                isActive = it
            }
        }
    }

    private fun applyActiveUI() {
        val primaryColor = getPrimaryColor(context)
        labelView.apply {
            background = GradientDrawable().apply {
                cornerRadius = 100f
                color = ColorStateList.valueOf(primaryColor)
                setStroke(1.dp, getAttr(context, android.R.attr.textColor))
            }
            setTextColor(Utils.getColorLuminance(primaryColor))
        }
    }
    private fun resetActiveUI() {
        labelView.apply {
            background = GradientDrawable().apply {
                cornerRadius = 100f
                color = ColorStateList.valueOf(Color.TRANSPARENT)
                setStroke(1.dp, getAttr(context, android.R.attr.textColor))
            }
            setTextColor(getAttr(context, android.R.attr.textColor))
        }
    }

    private fun getPrimaryColor(context: Context): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }
}