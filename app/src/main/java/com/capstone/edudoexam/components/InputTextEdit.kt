package com.capstone.edudoexam.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getAttr
import com.google.android.material.internal.TextWatcherAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

open class InputTextEdit @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val onChangeRecycler: MutableList<() -> Unit> = mutableListOf()
    private val textInputLayout: TextInputLayout by lazy {
        TextInputLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxCornerRadii(14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat())
        }
    }

    private val editText: TextInputEditText by lazy {
        TextInputEditText(textInputLayout.context).apply { // Use the context from textInputLayout
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setHintTextColor(getAttr(context, android.R.attr.textColorHint))
            setTextColor(getAttr(context, android.R.attr.textColor))

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    onChangeRecycler.forEach { it() }
                }
            })
        }
    }

    init {
        textInputLayout.addView(editText)
        addView(textInputLayout)

        context.theme.obtainStyledAttributes(attrs, R.styleable.InputTextEdit, 0, 0).apply {
            try {
                val hint = getString(R.styleable.InputTextEdit_hint)
                val inputType = getInt(R.styleable.InputTextEdit_inputType, InputType.TYPE_CLASS_TEXT)
                val startIcon = getDrawable(R.styleable.InputTextEdit_startIcon)
                val endIcon = getDrawable(R.styleable.InputTextEdit_endIcon)
                val maxLength = getInt(R.styleable.InputTextEdit_maxLength, -1)
                val maxLines = getInt(R.styleable.InputTextEdit_maxLines, -1)

                if (!hint.isNullOrEmpty()) {
                    textInputLayout.hint = hint
                }
                if (inputType != 0) {
                    editText.inputType = inputType
                }
                if (startIcon != null) {
                    textInputLayout.startIconDrawable = startIcon
                }
                if (endIcon != null) {
                    textInputLayout.endIconDrawable = endIcon
                    textInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                }
                if (maxLength != -1) {
                    editText.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
                }
                if (maxLines != -1) {
                    editText.setLines(maxLines)
                    editText.maxLines = maxLines
                }
            } finally {
                recycle()
            }
        }
    }

    fun onTextChanged(callback: () -> Unit) {
        onChangeRecycler.add(callback)
    }

    var hint: String
        get() = textInputLayout.hint.toString()
        set(value) {
            textInputLayout.hint = value
        }

    var text: String
        get() = editText.text.toString()
        set(value) {
            editText.setText(value)
        }

    var error: String
        get() = textInputLayout.error.toString()
        set(value) {
            textInputLayout.error = value
        }

    var inputType: Int
        get() = editText.inputType
        set(value) {
            editText.inputType = value
        }

    var startIcon: Drawable?
        get() = textInputLayout.startIconDrawable
        set(value) {
            textInputLayout.startIconDrawable = value
        }

    var endIcon: Drawable?
        get() = textInputLayout.endIconDrawable
        set(value) {
            textInputLayout.endIconDrawable = value
        }
    var maxLength: Int
        get() = editText.filters?.size ?: -0
        set(value) {
            editText.filters = arrayOf(android.text.InputFilter.LengthFilter(value))
        }
    var maxLines: Int
        get() = editText.maxLines
        set(value) {
            editText.setLines(value)
            editText.maxLines = value
        }

    var endIconMode: Int
        get() = textInputLayout.endIconMode
        set(value) {
            textInputLayout.endIconMode = value
        }
}