package com.capstone.edudoexam.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.models.QuestionOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class OptionsEditLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val checkIcon: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.baseline_check_circle_outline_24).apply {
            this?.setTint(ContextCompat.getColor(context, R.color.primary_light))
        }
    }
    private var onChangedCallback: ((EditText?) -> Unit)? = null

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


    var options: QuestionOptions
        get() = QuestionOptions(a = aText, b = bText, c = cText, d = dText)
        set(value) {
            aText = value.a
            bText = value.b
            cText = value.c
            dText = value.d
        }

    var correctOption: Char = 'A'
        set(value) {
            field = value
            spinnerOptions.setSelection(
                when (field) {
                    'A' -> 0
                    'B' -> 1
                    'C' -> 2
                    'D' -> 3
                    else -> 0
                }
            )
            highlightCorrectInput()
        }

    private val spinnerOptions: Spinner by lazy {
        Spinner(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(12.dp, 0, 0, 0)
            }
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Option A", "Option B", "Option C", "Option D")
            )
        }
    }

    private val spinnerContainer: LinearLayout by lazy {
        LinearLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 14.dp)
                setPadding(8.dp, 0, 8.dp, 0)
            }
            background = ContextCompat.getDrawable(context, R.drawable.rounded_frame)
            val label = TextView(context).apply {
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                text = "Correct Option:"
                textSize = 16f
                typeface = resources.getFont(R.font.montserrat_semi_bold)
                gravity = Gravity.CENTER_VERTICAL
            }
            addView(label)
            addView(spinnerOptions)
        }
    }

    init {

        orientation = VERTICAL
        addView(spinnerContainer)

        addRow('A', aEditText)
        addRow('B', bEditText)
        addRow('C', cEditText)
        addRow('D', dEditText)

        correctOption = 'A'
        spinnerOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                correctOption = when (position) {
                    0 -> 'A'
                    1 -> 'B'
                    2 -> 'C'
                    3 -> 'D'
                    else -> 'A'
                }

                highlightCorrectInput()
                onChangedCallback?.invoke(null) // Notify callback
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No-op
            }
        }

        highlightCorrectInput()
    }

    fun setOnChangedCallback(callback: (EditText?) -> Unit) {
        onChangedCallback = callback
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
            setOnFocusChangeListener { _, hasFocus ->
                //if (hasFocus) {
                this@OptionsEditLayout.onFocusChangeListener?.onFocusChange(this@OptionsEditLayout, hasFocus)
                //}
            }
        }

        textInputLayout.addView(editText)
        editText.doOnTextChanged { _, _, _, _ ->
            onChangedCallback?.invoke(editText)
        }
        return textInputLayout
    }

    @SuppressLint("SetTextI18n")
    private fun addRow(char: Char, view: View) {
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
                setMargins(0, 2.dp, 0, 0)
                setPadding(0, 14.dp, 0, 0)
            }
            text = "$char."
            textSize = 18f
            typeface = resources.getFont(R.font.montserrat_bold)
            gravity = Gravity.TOP
            inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }

        rowLayout.addView(labelTextView)
        rowLayout.addView(view)
        addView(rowLayout)
    }

    private fun highlightCorrectInput() {
        val editText = when (correctOption) {
            'A' -> aEditText
            'B' -> bEditText
            'C' -> cEditText
            'D' -> dEditText
            else -> null
        }
        clearHighlight()
        editText?.apply {
            requestFocus()
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            endIconDrawable = checkIcon
        }
    }

    private fun clearHighlight() {
        listOf(aEditText, bEditText, cEditText, dEditText).forEach {
            it.endIconMode = TextInputLayout.END_ICON_NONE
        }
    }

}