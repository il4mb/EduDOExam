package com.capstone.edudoexam.components

import android.content.Context
import android.content.res.Resources
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
    }
}