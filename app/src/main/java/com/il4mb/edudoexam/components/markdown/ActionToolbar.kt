package com.il4mb.edudoexam.components.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.layout.AutoWrapLayout
import com.il4mb.edudoexam.tools.Utils.Companion.dp

class ActionToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AutoWrapLayout(context, attrs, defStyleAttr) {

    private val activeIds: MutableList<Int> = mutableListOf()
    init {
        horizontalGap = 10.dp
        inflateMenu(R.menu.ritch_text_menu)
        updateUI()
    }

    private fun inflateMenu(menuRes: Int) {
        val menu = createMenu()
        val inflater = MenuInflater(context)
        inflater.inflate(menuRes, menu)

        // Add each menu item as a button to the LinearLayout
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val button = createButtonForMenuItem(menuItem)
            addView(button)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun createMenu(): Menu {
        val contextWrapper = ContextThemeWrapper(context, R.style.Theme_EduDOExam) // Use your app theme
        return androidx.appcompat.view.menu.MenuBuilder(contextWrapper)
    }

    private fun createButtonForMenuItem(menuItem: MenuItem): ImageView {
        return ImageView(context).apply {
            id = menuItem.itemId
            setOnClickListener { handleMenuItemClick(menuItem) }
            setImageDrawable(menuItem.icon)
            layoutParams = LayoutParams(
                55.dp,
                40.dp
            )
            setPadding(10.dp, 10.dp, 10.dp, 10.dp)
        }
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        val id = menuItem.itemId
        if (activeIds.contains(id)) {
            activeIds.remove(id)
        } else {
            activeIds.add(id)
        }
        updateUI()
    }

    fun isActive(id: Int): Boolean {
        return activeIds.contains(id)
    }

    private fun updateUI() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is ImageView) {
                val id = child.id
                child.apply {
                    background = if (isActive(id)) {
                        alpha = 1f
                        imageTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.white
                            )
                        )
                        GradientDrawable().apply {
                            color = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    context,
                                    R.color.primary
                                )
                            )
                            cornerRadius = 10.dp.toFloat()
                        }

                    } else {
                        alpha = 0.4f
                        imageTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.black
                            )
                        )
                        null
                    }
                }
            }
        }
    }

}