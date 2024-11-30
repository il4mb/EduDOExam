package com.capstone.edudoexam.components

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.capstone.edudoexam.R
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE

class InputPasswordLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : InputTextEdit(context, attrs, defStyleAttr)  {

    init {
        hint = "Password"
        endIconMode = END_ICON_PASSWORD_TOGGLE
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        startIcon   = ContextCompat.getDrawable(context, R.drawable.baseline_lock_24)

        onTextChanged {
            if (text.length < 8) {
                error = "Password must be at least 8 characters"
            } else {
                error = ""
            }
        }
    }
}