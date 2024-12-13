package com.il4mb.edudoexam.components.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.il4mb.edudoexam.R

class AppDialog(private val activity: FragmentActivity) {

    private val dialog: AppDialogFragment by lazy {
        AppDialogFragment()
    }

    fun setTitle(title: String): AppDialog {
        dialog.arguments = (dialog.arguments ?: Bundle()).apply {
            putString(AppDialogFragment.ARG_TITLE, title)
        }
        return this
    }

    fun setMessage(message: String): AppDialog {
        dialog.arguments = (dialog.arguments ?: Bundle()).apply {
            putString(AppDialogFragment.ARG_MESSAGE, message)
        }
        return this
    }

    fun setPositiveButton(text: String, onClick: () -> Unit): AppDialog {
        dialog.setPositiveButton(text, onClick)
        return this
    }

    fun show() {
        if (!dialog.isAdded) {
            dialog.show(activity.supportFragmentManager, "AppDialog")
        }
    }

    fun dismiss() {
        if (dialog.isAdded) {
            dialog.dismiss()
        }
    }

    class AppDialogFragment : DialogFragment() {

        companion object {
            const val ARG_TITLE = "ARG_TITLE"
            const val ARG_MESSAGE = "ARG_MESSAGE"
        }

        private var positiveButtonAction: (() -> Unit)? = null

        fun setPositiveButton(text: String, onClick: () -> Unit) {
            arguments = (arguments ?: Bundle()).apply {
                putString("positive_button_text", text)
            }
            positiveButtonAction = onClick
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val context = requireContext()

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                background = ContextCompat.getDrawable(context, R.drawable.rounded_frame_outline)
            }

            val title = arguments?.getString(ARG_TITLE)
            val message = arguments?.getString(ARG_MESSAGE)
            val positiveText = arguments?.getString("positive_button_text") ?: "OK"

            title?.let {
                val titleView = TextView(context).apply {
                    text = it
                    textSize = 20f
                }
                layout.addView(titleView)
            }

            message?.let {
                val messageView = TextView(context).apply {
                    text = it
                    textSize = 16f
                }
                layout.addView(messageView)
            }

            val positiveButton = TextView(context).apply {
                text = positiveText
                textSize = 16f
                setPadding(0, 16, 0, 0)
                setOnClickListener {
                    positiveButtonAction?.invoke()
                    dismiss()
                }
            }
            layout.addView(positiveButton)

            return layout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
