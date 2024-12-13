package com.il4mb.edudoexam.components.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.Utils.Companion.getAttr

class InfoDialog(private val activity: FragmentActivity) {

    companion object {
        const val LENGTH_LONG = 1
        const val LENGTH_SHORT = 0
        const val LENGTH_INDEFINITE = -1
    }

    private val dialog: AppDialogFragment by lazy {
        AppDialogFragment()
    }

    fun setTitle(title: String): InfoDialog {
        dialog.arguments = (dialog.arguments ?: Bundle()).apply {
            putString(AppDialogFragment.ARG_TITLE, title)
        }
        return this
    }

    fun setMessage(message: String): InfoDialog {
        dialog.arguments = (dialog.arguments ?: Bundle()).apply {
            putString(AppDialogFragment.ARG_MESSAGE, message)
        }
        return this
    }

    fun show(length: Int = LENGTH_SHORT) {
        try {
            dismissAllDialogs()
            dialog.setLength(length)
            if (!dialog.isAdded) {
                dialog.show(activity.supportFragmentManager, "InfoDialog")
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun dismiss() {
        if (dialog.isAdded) {
            dialog.dismiss()
        }
    }

    private fun dismissAllDialogs() {

        val fragmentManager = activity.supportFragmentManager
        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is AppDialogFragment && fragment.isVisible) {
                fragment.dismiss()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    class AppDialogFragment : DialogFragment() {

        companion object {
            const val ARG_TITLE = "ARG_TITLE"
            const val ARG_MESSAGE = "ARG_MESSAGE"

        }
        private var length: Int = LENGTH_SHORT

        fun setLength(length: Int) {
            this.length = length
        }

        private val wrappers: LinearLayout by lazy {
            LinearLayout(context).apply {
                clipToPadding = false
                setPadding(32, 50, 32, 50)
                gravity = Gravity.CENTER
            }
        }

        private val layout: LinearLayout by lazy {
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18.dp, 18.dp, 18.dp, 18.dp)
                background = ContextCompat.getDrawable(context, R.drawable.dialog_info_bg)
                elevation = 5f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val context = requireContext()

            val title = arguments?.getString(ARG_TITLE)
            val message = arguments?.getString(ARG_MESSAGE)

            title?.let {
                val titleView = TextView(context).apply {
                    text = it
                    textSize = 18f
                    setTextColor(getAttr(context, android.R.attr.colorBackground))
                    typeface = resources.getFont(R.font.montserrat_semi_bold)
                }
                layout.addView(titleView)
            }

            message?.let {
                val messageView = TextView(context).apply {
                    text = it
                    textSize = 14f
                    setTextColor(getAttr(context, android.R.attr.colorBackground))
                    typeface = resources.getFont(R.font.montserrat_regular)
                }
                layout.addView(messageView)
            }

            wrappers.addView(layout)

            return wrappers
        }

        @Suppress("DEPRECATION")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            animateIn(layout)
            dialog?.window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                setGravity(Gravity.TOP)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }

            when (length) {
                LENGTH_SHORT -> view.postDelayed({ dismissIfAdded(layout) }, 2000L)
                LENGTH_LONG -> view.postDelayed({ dismissIfAdded(layout) }, 5000L)
            }

        }

        override fun onStart() {
            super.onStart()
            dialog?.window?.apply {

                setWindowAnimations(R.style.SlideDialogAnimation)
                setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                )
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

                val params = attributes
                params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                params.y = 0
                attributes = params

                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            }
        }

        private fun dismissIfAdded(view: View) {
            if (isAdded) {
                animateOut(view) {
                    dismiss()
                }
            }
        }

        private fun animateIn(view: View) {
            view.apply {
                translationY += 30
                alpha = 0f
                animate()
                    .setStartDelay(400)
                    .alpha(1f)
                    .translationY(0f)
                    .duration = 400
            }
        }

        private fun animateOut(view: View, callback: () -> Unit) {
            view.apply {
                animate()
                    .alpha(0f)
                    .translationY(-20f) // Move slightly up as it disappears
                    .withEndAction { callback() }
                    .duration = 100 // Adjust duration as needed
            }
        }

        override fun dismiss() {
            animateOut(layout) {
                try {
                    super.dismiss()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }
}
