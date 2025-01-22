package com.il4mb.edudoexam.components.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.Gravity.CENTER_VERTICAL
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.input.TextInput
import com.il4mb.edudoexam.tools.Utils.Companion.dp

class QuestionOption @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout( context, attrs, defStyleAttr) {

    private val textInput: TextInput by lazy {
        TextInput(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            prefixText = "A."
            boxStrokeColor = Color.TRANSPARENT
        }
    }

    private val endIcon: ImageView by lazy {
        ImageView(context).apply {
            layoutParams = LayoutParams(30.dp, 30.dp).apply {
                marginStart = 10.dp
            }
            setImageResource(R.drawable.three_lines)
            setOnClickListener {
                FloatingMenu(context, this).apply {
                    xOffset = -400
                    yOffset = 100
                    addItem("Mark as correct").apply {
                        color = Color.GREEN
                        icon = context.getDrawable(R.drawable.baseline_check_24)
                        setOnClickListener {  }
                    }
                    addItem("Delete").apply {
                        color = Color.RED
                        icon = context.getDrawable(R.drawable.baseline_delete_24)
                        setOnClickListener {  }
                    }
                    onDismissCallback = {
                        setImageResource(R.drawable.three_lines)
                    }
                    onShowCallback = {
                        setImageResource(R.drawable.baseline_close_24)
                    }

                }.show()
            }
        }
    }

    var text: String
        get() = textInput.editText?.text.toString()
        set(value) {
            textInput.editText?.setText(value)
        }

    init {
        orientation = HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        gravity = CENTER_VERTICAL

        addView(textInput)
        addView(endIcon)
    }
}