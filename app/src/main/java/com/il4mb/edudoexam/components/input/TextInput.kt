package com.il4mb.edudoexam.components.input

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ActionMode
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.il4mb.edudoexam.tools.Utils.Companion.dp
import com.il4mb.edudoexam.tools.Utils.Companion.getAttr

class TextInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    var customInsertionActionModeCallback: ActionMode.Callback? = null
        set(value) {
            field = value
            editText.customInsertionActionModeCallback = value
        }

    private val editText: TextInputEditText

    var text: CharSequence
        get() = editText.text ?: ""
        set(value) {
            if (editText.text.toString() != value.toString()) {
                editText.setText(value)
                editText.setSelection(value.length)
            }
        }

    private val onTextChangedListeners: MutableList<(String) -> Unit> = mutableListOf()

    init {
        // Configure TextInputLayout
        setBoxCornerRadii(14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat())
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        // Create and add TextInputEditText
        editText = TextInputEditText(this.context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            gravity = this@TextInput.gravity
            minHeight = this@TextInput.minimumHeight
            setHintTextColor(getAttr(context, android.R.attr.textColorHint))

            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    onTextChangedListeners.forEach { it.invoke(s.toString()) }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }
            })
        }
        addView(editText) // Add the editText to the TextInputLayout

    }

    /**
     * Adds a listener for text changes.
     */
    fun onTextChanged(callback: (String) -> Unit) {
        onTextChangedListeners.add(callback)
    }

    /**
     * Clears all registered text change listeners.
     */
    fun clearTextChangedListeners() {
        onTextChangedListeners.clear()
    }

}
