package com.il4mb.edudoexam.components.input

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.Utils.Companion.getAttr
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

open class InputTextEdit @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var editable: Boolean = true
        set(value) {
            field = value
            editText.isEnabled = value
            textInputLayout.alpha = if (value) 1f else 0.75f
        }

    private val onChangeRecycler: MutableList<() -> Unit> = mutableListOf()
    private val textInputLayout: TextInputLayout by lazy {
        TextInputLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxCornerRadii(14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat())
        }
    }
    private val editText: TextInputEditText by lazy {
        TextInputEditText(textInputLayout.context).apply { // Use the context from textInputLayout
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
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
                val enabled = getBoolean(R.styleable.InputTextEdit_enabled, true)
                editable = getBoolean(R.styleable.InputTextEdit_editable, true)


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
                    textInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    textInputLayout.endIconDrawable = endIcon
                }
                if (maxLength != -1) {
                    editText.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
                }
                if (maxLines != -1) {
                    editText.setLines(maxLines)
                    editText.maxLines = maxLines
                }
                if (!enabled) {
                    setEnabled(false)
                }
            } finally {
                recycle()
            }
        }
    }

    fun onTextChanged(callback: () -> Unit) {
        onChangeRecycler.add(callback)
    }

    fun setSelection(pos: Int) {
        editText.setSelection(pos)
    }

    fun getLayout() : TextInputLayout {
        return textInputLayout
    }

    fun addTextChangedListener(textWatcher: TextWatcher) {
        editText.addTextChangedListener(textWatcher)
    }

    fun onClickAtEnd(callback: () -> Unit) {
        textInputLayout.setEndIconOnClickListener {
            callback()
        }
    }

    fun onClickAtStart(callback: () -> Unit) {
        textInputLayout.setStartIconOnClickListener {
            callback()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        editText.isEnabled = enabled
        textInputLayout.isEnabled = enabled
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // Ensure the TextInputLayout stretches the full height
        textInputLayout.layout(0, 0, width, height)
    }

    override fun setOnKeyListener(l: OnKeyListener?) {
        editText.setOnKeyListener(l)
        super.setOnKeyListener(l)
    }

    var typeface: android.graphics.Typeface?
        get() = editText.typeface
        set(value) {
            editText.typeface = value
        }

    var textSize: Float
        get() = editText.textSize
        set(value) {
            editText.textSize = value
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

    var helperText: CharSequence?
        get() = textInputLayout.helperText
        set(value) {
            textInputLayout.helperText = value
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