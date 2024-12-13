package com.il4mb.edudoexam.components.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.Utils.Companion.getAttr

class FloatingMenu(
    private val context: Context,
    private val anchor: View
) {

    private val items: ArrayList<FloatingMenuItem> = arrayListOf()
    var xOffset = 0
    var yOffset = 0
    var onDismissCallback: (() -> Unit)? = null

    private val roundedBackground: GradientDrawable by lazy {
        GradientDrawable().apply {
            val bgColor = if(Utils.isDarkMode(context))
                getAttr(context, android.R.attr.colorBackground)
            else
                getAttr(context, android.R.attr.colorBackground)
            val strokeColor = getAttr(context, android.R.attr.textColor)

            color = ColorStateList.valueOf(bgColor)
            cornerRadius = 16f
            setStroke(1.dp, Color.argb(50, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor)))
        }
    }

    private val container: LinearLayout by lazy {
        LinearLayout(context).apply {
            background = roundedBackground
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(12.dp, 8.dp, 12.dp,8.dp)
                elevation = 4f
            }
        }
    }

    private val popUp: PopupWindow by lazy {
        PopupWindow(anchor).apply {
            contentView = container
            width = LinearLayout.LayoutParams.WRAP_CONTENT
            height = LinearLayout.LayoutParams.WRAP_CONTENT
            isFocusable = true
            animationStyle = R.style.PopupAnimationStyle
            elevation = 4f

            setOnDismissListener {
                onDismissCallback?.invoke()
            }
            setBackgroundDrawable(ColorDrawable())
        }
    }

    fun addItem(text: String) : FloatingMenuItem {
        return FloatingMenuItem(context).apply {
            this.text = text
            items.add(this)
        }
    }

    private fun renderChildren() {
        container.removeAllViews()
        items.forEach {
            container.addView(it)
        }
    }

    fun show() {

        renderChildren()
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        popUp.showAtLocation(container, Gravity.NO_GRAVITY, location[0] + xOffset, location[1] + yOffset)
    }

    fun hide() {
        container.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(200)
            .withEndAction { popUp.dismiss() }
            .start()
    }

    class FloatingMenuItem(context: Context): LinearLayout(context), View.OnTouchListener {

        private val prefixIcon: ImageView by lazy {
            ImageView(context).apply {
                layoutParams = LayoutParams(25.dp, 25.dp).apply {
                    setMargins(0,0, 12.dp, 0)
                }
                gravity = Gravity.CENTER_VERTICAL
                visibility = View.GONE
            }
        }
        var icon: Drawable? = null
            set(value) {
                field = value?.mutate()
                if(field != null) {
                    prefixIcon.apply {
                        DrawableCompat.setTint(field!!, color)
                        setImageDrawable(icon)
                        visibility = View.VISIBLE
                    }
                } else {
                    prefixIcon.apply {
                        setImageDrawable(icon)
                        visibility = View.GONE
                    }
                }
            }

        private val textView: TextView by lazy {
            TextView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setPadding(12.dp, 5.dp, 12.dp, 5.dp)
                }
                gravity = Gravity.CENTER_VERTICAL
                typeface = resources.getFont(R.font.montserrat_semi_bold)
            }
        }
        var text: String = ""
            set(value) {
                field = value
                textView.text = field
            }

        @ColorInt
        var color: Int = getAttr(context, android.R.attr.textColor)
            set(value) {
                field = value
                textView.setTextColor(field)
                prefixIcon.drawable.apply {
                    try {
                        DrawableCompat.setTint(this, field)
                    } catch (_: Throwable) {
                        // silent is perfect
                    }
                }
            }

        init {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setPadding(12.dp, 6.dp, 12.dp, 6.dp)
            }
            orientation = HORIZONTAL
            addView(prefixIcon)
            addView(textView)

            setOnTouchListener(this)
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {

            animate()
                .setDuration(150)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .withEndAction {
                    animate()
                        .setDuration(200)
                        .scaleX(1f)
                        .scaleY(1f)
                        .start()
                }
                .start()

            return false
        }
    }
}
