package com.capstone.edudoexam.components

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import androidx.core.content.ContextCompat

class Utils {

    companion object {

        val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
        val Int.px: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

        fun isDarkMode(context: Context): Boolean {
            val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        fun getAttr(context: Context, attr: Int): Int {
            val typedValue = TypedValue()
            val theme = context.theme
            if (theme.resolveAttribute(attr, typedValue, true)) {
                return if (typedValue.resourceId != 0) {
                    ContextCompat.getColor(context, typedValue.resourceId) // Get color from resource
                } else {
                    typedValue.data // Return raw color value
                }
            }
            return 0
        }

        fun getColor(context: Context, color: Int): Int {
            return ContextCompat.getColor(context, color)
        }

        fun getColorLuminance(color: Int): Int {
            val r = Color.red(color) / 255.0
            val g = Color.green(color) / 255.0
            val b = Color.blue(color) / 255.0

            val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
            return if (luminance > 0.5) Color.BLACK else Color.WHITE
        }

    }
}