package com.il4mb.edudoexam.ui.dashboard.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.LinearLayout
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.input.InputTextEdit
import com.google.android.material.textfield.TextInputLayout

@SuppressLint("UseCompatLoadingForDrawables")
class JoinExamFormAppbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val textInputLayout: InputTextEdit by lazy {
        InputTextEdit(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            hint = context.getString(R.string.join_exam)
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            typeface = resources.getFont(R.font.montserrat_bold)
            textSize = 18f
            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            getLayout().apply {
                boxStrokeWidth = 3.dp
                val colorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
                        intArrayOf(android.R.attr.state_enabled),
                        intArrayOf()
                    ),
                    intArrayOf(
                        context.getColor(R.color.input_box_outline_focus),
                        context.getColor(R.color.input_box_outline_hover),
                        context.getColor(R.color.input_box_outline)
                    )
                )

                setBoxStrokeColorStateList(colorStateList)
                defaultHintTextColor = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
                        intArrayOf(android.R.attr.state_enabled),
                        intArrayOf()
                    ),
                    intArrayOf(
                        context.getColor(R.color.input_box_outline_focus),
                        context.getColor(R.color.input_box_outline),
                        context.getColor(R.color.input_box_outline)
                    )
                )
                boxStrokeWidthFocused = 4.dp
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                    charSequence?.let {
                        val newText = it.toString().replace(" ", "")
                        if (it.toString() != newText) {
                            post {
                                textInputLayout.text = newText
                                textInputLayout.setSelection(newText.length)
                            }
                        }
                    }
                }

                override fun afterTextChanged(editable: Editable?) {}
            })
        }
    }


    init {
        // Set layout orientation and padding
        orientation = HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setPadding(
            resources.getDimensionPixelSize(R.dimen.dp_14),
            resources.getDimensionPixelSize(R.dimen.dp_14),
            resources.getDimensionPixelSize(R.dimen.dp_14),
            resources.getDimensionPixelSize(R.dimen.dp_18)
        )

        // Add the TextInputLayout to the LinearLayout
        addView(textInputLayout)
    }
}