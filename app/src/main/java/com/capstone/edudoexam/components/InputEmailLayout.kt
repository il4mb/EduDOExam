package com.capstone.edudoexam.components

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.capstone.edudoexam.R
import com.google.android.material.textfield.TextInputLayout

class InputEmailLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : InputTextEdit(context, attrs, defStyleAttr) {

    init {
        hint = "Email"
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        maxLines  = 1
        maxLength = 100
        startIcon = ContextCompat.getDrawable(context, R.drawable.baseline_email_24)
        endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT

        onTextChanged {
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                error = "Invalid email address"
            } else {
                error = ""
            }
        }
    }
}
