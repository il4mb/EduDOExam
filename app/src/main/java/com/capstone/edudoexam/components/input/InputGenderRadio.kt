package com.capstone.edudoexam.components.input

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.getAttr

class InputGenderRadio @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var onGenderChanged: (() -> Unit)? = null
    fun onGenderChanged(function: () -> Unit) {
        onGenderChanged = function
    }

    private val titleView: TextView by lazy {
        TextView(context).apply {
            text = resources.getString(R.string.gender)
            textSize = 16f
            setTextColor(getAttr(context, android.R.attr.textColor))
        }
    }

    private val radioGroup: RadioGroup by lazy {
        RadioGroup(context).apply {
            orientation = RadioGroup.HORIZONTAL
        }
    }

    private val radioButtonMale: RadioButton by lazy {
        RadioButton(context).apply {
            text = resources.getString(R.string.gender_male)
        }
    }

    private val radioButtonFemale: RadioButton by lazy {
        RadioButton(context).apply {
            text = resources.getString(R.string.gender_female)
            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(30, 0, 0, 0)
            }
        }
    }

    var gender: Int
        get() = when {
            radioButtonMale.isChecked -> 1
            radioButtonFemale.isChecked -> 0
            else -> -1
        }
        set(value) {
            when (value) {
                1 -> radioButtonMale.isChecked = true
                0 -> radioButtonFemale.isChecked = true
            }
        }

    init {
        orientation = VERTICAL
        addView(titleView)
        addView(radioGroup)
        radioGroup.addView(radioButtonMale)
        radioGroup.addView(radioButtonFemale)

        context.theme.obtainStyledAttributes(attrs, R.styleable.InputGenderRadio, defStyleAttr, 0).apply {
            try {
                val gender = getInt(R.styleable.InputGenderRadio_gender, 1)
                when (gender) {
                    1 -> radioButtonMale.isChecked = true
                    0 -> radioButtonFemale.isChecked = true
                }
            } finally {
                recycle()
            }
        }

        radioGroup.setOnCheckedChangeListener { _, _ ->
            onGenderChanged?.invoke()
        }
    }
}
