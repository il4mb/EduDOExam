package com.capstone.edudoexam.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.capstone.edudoexam.R
import com.capstone.edudoexam.models.QuestionOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class OptionsEditLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val aEditText: TextInputLayout by lazy {
        createTextInput()
    }
    var aText: String
        get() = aEditText.editText?.text.toString()
        set(value) {
            aEditText.editText?.setText(value)
        }

    private val bEditText: TextInputLayout by lazy {
        createTextInput()
    }
    var bText: String
        get() = bEditText.editText?.text.toString()
        set(value) {
            bEditText.editText?.setText(value)
        }

    private val cEditText: TextInputLayout by lazy {
        createTextInput()
    }
    var cText: String
        get() = cEditText.editText?.text.toString()
        set(value) {
            cEditText.editText?.setText(value)
        }

    private val dEditText: TextInputLayout by lazy {
        createTextInput()
    }
    var dText: String
        get() = dEditText.editText?.text.toString()
        set(value) {
            dEditText.editText?.setText(value)
        }


    init {
        orientation = VERTICAL
        addRow('A', aEditText)
        addRow('B', bEditText)
        addRow('C', cEditText)
        addRow('D', dEditText)
    }


    fun setOptions(options: QuestionOptions) {
        aText = options.a
        bText = options.b
        cText = options.c
        dText = options.d
    }

    private fun createTextInput(): TextInputLayout {

        val textInputLayout = TextInputLayout(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(16, 0, 0, 0)
            }

            typeface = resources.getFont(R.font.montserrat)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            placeholderText = "Enter options"
        }

        val editText = TextInputEditText(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            background = ColorDrawable()
        }

        textInputLayout.addView(editText)
        return textInputLayout
    }


    @SuppressLint("SetTextI18n")
    private fun addRow(char: Char, textInputLayout: TextInputLayout) {
        val rowLayout = LinearLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 22)
                setPadding(15.dp, 0, 15.dp, 0)
            }
            gravity = Gravity.CENTER_VERTICAL
            background = ContextCompat.getDrawable(context, R.drawable.rounded_frame)
        }
        val labelTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
            ).apply {
                setPadding(0, 14.dp, 0, 0)
            }
            text = "$char."
            textSize = 18f
            typeface = resources.getFont(R.font.montserrat_bold)
            gravity = Gravity.TOP
            inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }

        rowLayout.addView(labelTextView)
        rowLayout.addView(textInputLayout)
        addView(rowLayout)
    }

    private val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()

}