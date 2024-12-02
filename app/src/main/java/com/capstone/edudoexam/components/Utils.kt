package com.capstone.edudoexam.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getAttr
import com.google.android.material.snackbar.Snackbar as DefaultSnackbar

class Utils {

    companion object {

        val String.CountWords: Int get() {
                val words = this.split("\\s+".toRegex())
                    .filter { it.length >= 3 }
                return words.size
            }
        val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
        val Int.px: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

        fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetwork?.let { network ->
                connectivityManager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } == true
        }


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

        fun hideKeyboard(activity: Activity) {
            val view = activity.currentFocus ?: View(activity)
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun copyTextToClipboard(context: Context, textToCopy: String) : Boolean {
            if (textToCopy.isNotBlank()) {
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", textToCopy)
                clipboardManager.setPrimaryClip(clip)
                return true
            }
            return false
        }
    }
}

class Snackbar private constructor() {

    private var anchorView: View? = null

    companion object {
        fun with(view: View): Snackbar {
            return Snackbar().apply {
                anchorView = view
            }
        }

        const val LENGTH_SHORT = DefaultSnackbar.LENGTH_SHORT
        const val LENGTH_LONG = DefaultSnackbar.LENGTH_LONG
    }

    fun show(message: String, length: Int) {

        anchorView?.apply {
            val snackbar = DefaultSnackbar.make(this, message, length)
            snackbar.view.post {
                (snackbar.view as FrameLayout).apply {
                    background = ContextCompat.getDrawable(this.context, R.drawable.rounded_frame)
                }
                val snackbarHeight = snackbar.view.height
                val translationY = (snackbarHeight*2)
                snackbar.view.y = translationY.toFloat()
            }
            snackbar.show()
        }
    }

    @SuppressLint("RestrictedApi")
    fun show(title: String, message: String, length: Int) {
        anchorView?.apply {
            val snackbar = DefaultSnackbar.make(this, "", length) // Empty message since we're replacing the view

            snackbar.view.post {
                val context = this.context

                val titleView = TextView(context).apply {
                    text = title
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    setTextColor(getAttr(context, android.R.attr.colorBackground))
                    typeface = ResourcesCompat.getFont(context, R.font.montserrat_semi_bold)
                }

                val messageView = TextView(context).apply {
                    text = message
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(getAttr(context, android.R.attr.colorBackground))
                }

                val customLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16.dp, 16.dp, 16.dp, 16.dp)
                    addView(titleView)
                    addView(messageView)
                }

                (snackbar.view as DefaultSnackbar.SnackbarLayout).apply {
                    addView(customLayout, 0)
                }

                // Adjust Y position (optional - this centers Snackbar vertically)
                snackbar.view.post {
                    val snackbarHeight = snackbar.view.height
                    snackbar.view.y = ((snackbarHeight * 2) - (snackbarHeight / 4)).toFloat()
                }
            }

            snackbar.show()
        }
    }

}