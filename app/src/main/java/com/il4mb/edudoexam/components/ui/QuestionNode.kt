package com.il4mb.edudoexam.components.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorInt
import com.il4mb.edudoexam.R
import com.google.android.material.card.MaterialCardView


class QuestionNode(
    context: Context
) : MaterialCardView(context) {

    @ColorInt
    var mainColor: Int = Color.parseColor("#5E5E5E")
        set(value) {
            field = value
            strokeColor = value
            textView.setTextColor(value)
            setCardBackgroundColor(getBackgroundColor(value))
        }

    private var _text: String = ""
    var text: String
        get() = _text
        set(value) {
            _text = value
            textView.text = value
        }

    private var _questionInfoWindow: QuestionInfoWindow =
        QuestionInfoWindow.inflate(LayoutInflater.from(context))

    var questionInfoWindow: QuestionInfoWindow
        get() = _questionInfoWindow
        set(value) {
            _questionInfoWindow = value
        }



    private val textView: TextView by lazy {
        TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = 16f
            typeface = resources.getFont(R.font.montserrat_semi_bold)
            setTextColor(mainColor)
        }
    }

    init {
        addView(textView)

        radius = 16.dp.toFloat()
        strokeWidth = 2.dp
        strokeColor = mainColor
        setCardBackgroundColor(getBackgroundColor(mainColor))
    }

    private fun getBackgroundColor(@ColorInt color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb((0.25f * 255).toInt(), r, g, b)
    }

    @SuppressLint("ServiceCast")
    fun showInfoWindow() {

        questionInfoWindow.let { binding ->
            val popUp = PopupWindow(this).apply {
                contentView = binding.root
                width = 200.dp
                height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                isFocusable = true
                setBackgroundDrawable(ColorDrawable())
            }

            val location = IntArray(2)
            getLocationOnScreen(location)

            popUp.showAtLocation(binding.root, Gravity.NO_GRAVITY, location[0], location[1])

        }
    }

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}
