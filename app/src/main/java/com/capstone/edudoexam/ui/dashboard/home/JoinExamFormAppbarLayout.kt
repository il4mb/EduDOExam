package com.capstone.edudoexam.ui.dashboard.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.google.android.material.textfield.TextInputLayout

@SuppressLint("UseCompatLoadingForDrawables")
class JoinExamFormAppbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val textInputLayout: TextInputLayout by lazy {
        TextInputLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            hint = "Join Exam"
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            endIconDrawable = context.getDrawable(R.drawable.outline_prompt_suggestion_24)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE

            setBoxCornerRadii(14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat())
            addView(EditText(this.context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                setPadding(12.dp, 0, 12.dp, 0)
                //background = ContextCompat.getDrawable(context, R.drawable.rounded_frame)
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