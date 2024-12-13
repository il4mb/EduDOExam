package com.il4mb.edudoexam.components.input

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.il4mb.edudoexam.R
import com.google.android.material.textfield.TextInputLayout

class InputEmailLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : InputTextEdit(context, attrs, defStyleAttr) {

    val isValid: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()

    init {
        hint = "Email"
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        maxLines  = 1
        maxLength = 100
        startIcon = ContextCompat.getDrawable(context, R.drawable.baseline_email_24)
        endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT

        onTextChanged {
            error = if(!isValid) {
                "Invalid email address"
            } else {
                ""
            }
        }
    }
}
