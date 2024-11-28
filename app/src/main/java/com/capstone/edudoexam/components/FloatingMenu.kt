package com.capstone.edudoexam.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getAttr

class FloatingMenu(
    private val context: Context,
    private val anchor: View
) {

    private val items: ArrayList<FloatingMenuItem> = arrayListOf()
    var xOffset = 100
    var yOffset = 200

    private val roundedBackground: GradientDrawable by lazy {
        GradientDrawable().apply {
            color = ColorStateList.valueOf(if(Utils.isDarkMode(context))
                Color.parseColor("#111111")
            else
                Color.parseColor("#DDDDDD")
            )
            cornerRadius = 16f
            setStroke(1.dp, Color.argb(0.5f, 0.5f,0.5f,0.5f))
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
            animationStyle = R.style.popup_window_animation
            elevation = 4f

            setBackgroundDrawable(ColorDrawable())
        }
    }


    fun addItem(text: String) : FloatingMenuItem {
        return FloatingMenuItem(context).apply {
            this.text = text
            items.add(this)
        }
    }

    private fun inflateItem() {
        container.removeAllViews()
        items.forEach {
            container.addView(it)
        }
    }


    fun show() {

        inflateItem()
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        popUp.showAtLocation(container, Gravity.NO_GRAVITY, location[0] + xOffset, location[1] + yOffset)
    }

    class FloatingMenuItem(context: Context): LinearLayout(context) {

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
                        DrawableCompat.setTint(field!!, getAttr(context, android.R.attr.textColor))
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


        init {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setPadding(12.dp, 6.dp, 12.dp, 6.dp)
            }
            orientation = HORIZONTAL
            addView(prefixIcon)
            addView(textView)

        }
    }
}
