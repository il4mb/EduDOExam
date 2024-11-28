package com.capstone.edudoexam.components

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

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
            context.theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }
    }
}