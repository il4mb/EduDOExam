package com.capstone.edudoexam.components

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
import com.capstone.edudoexam.R
import com.google.android.material.card.MaterialCardView


class QuestionNode(
    context: Context
) : MaterialCardView(context) {

    @ColorInt
    var mainColor: Int = Color.parseColor("#0075DD")
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

    private var _questionInfoWindow: QuestionInfoWindow = QuestionInfoWindow.inflate(LayoutInflater.from(context))
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
            setTextColor(mainColor)
        }
    }

    init {
        addView(textView)

        // Card styling
        radius = 16.dp.toFloat()
        strokeWidth = 4.dp
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
                width = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                isFocusable = true
                animationStyle = R.style.popup_window_animation
                setBackgroundDrawable(ColorDrawable())
            }

            val location = IntArray(2)
            getLocationOnScreen(location)

            val xOffset = 0
            val yOffset = -200

            popUp.showAtLocation(binding.root, Gravity.NO_GRAVITY, location[0] + xOffset, location[1] + yOffset)
        }
    }

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}
