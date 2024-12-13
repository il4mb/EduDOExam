package com.il4mb.edudoexam.components.input

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.il4mb.edudoexam.R
import com.google.android.material.textfield.TextInputLayout

class InputNumberLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : InputTextEdit(context, attrs, defStyleAttr) {

    init {
        hint = "Number"
        inputType = InputType.TYPE_NUMBER_VARIATION_NORMAL
        maxLines  = 1
        maxLength = 100
        startIcon   = ContextCompat.getDrawable(context, R.drawable.baseline_remove_24)
        endIconMode = TextInputLayout.END_ICON_CUSTOM
        endIcon     = ContextCompat.getDrawable(context, R.drawable.baseline_add_24)

        onClickAtEnd {
            val current = text.toIntOrNull() ?: 0
            text = (current + 1).toString()
        }
        onClickAtStart {
            val current = text.toIntOrNull() ?: 0
            text = (current - 1).toString()
        }
        onTextChanged {
            val current = text.toIntOrNull() ?: 0
            if (current < 0) text = "0"
        }
    }
}
