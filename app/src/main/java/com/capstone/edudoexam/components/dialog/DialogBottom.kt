package com.capstone.edudoexam.components.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getAttr
import com.capstone.edudoexam.components.Utils.Companion.getColorLuminance
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import java.lang.reflect.Method

class DialogBottom : BottomSheetDialogFragment() {

    var themeColor: Int = 0
        set(value) {
            field = value
            updateUI()
        }

    var title: String = ""
        set(value) {
            field = value
            updateUI()
        }

    var message: String = ""
        set(value) {
            field = value
            updateUI()
        }

    var dismissText: String = "Cancel"
        set(value) {
            field = value
            updateUI()
        }

    var dismissHandler: ((DialogBottom) -> Boolean)? = null

    var acceptText: String = "Accept"
        set(value) {
            field = value
            updateUI()
        }

    var acceptHandler: ((DialogBottom) -> Boolean)? = null

    var isCancelActionButtonVisible = true
        set(value) {
            field = value
            updateUI()
        }

    var isAcceptActionButtonVisible = true
        set(value) {
            field = value
            updateUI()
        }

    private var viewBinding: ViewBinding? = null
        set(value) {
            field = value
            updateUI()
        }

    private var layoutBindHelper: LayoutBindHelper<ViewBinding>? = null

    private val layout: DialogContainer by lazy {
        DialogContainer(requireContext()).apply {
            cancelButton.setOnClickListener {
                if(dismissHandler != null) {
                    if (dismissHandler!!.invoke(this@DialogBottom)) {
                        dismiss()
                    }
                } else {
                    dismiss()
                }
            }
            acceptButton.setOnClickListener {
                if(acceptHandler?.invoke(this@DialogBottom) == true) {
                    dismiss()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.setOnApplyWindowInsetsListener { v, insets ->
                val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(
                    systemInsets.left,
                    systemInsets.top,
                    systemInsets.right,
                    systemInsets.bottom
                )
                insets
            }
        }
        updateUI()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            enableEdgeToEdge()
        }

        return dialog
    }

    private fun Window.enableEdgeToEdge() {
        // Disable decor fitting system windows
        WindowCompat.setDecorFitsSystemWindows(this, false)

        // Set up the insets controller
        val insetsController = WindowInsetsControllerCompat(this, decorView)
        insetsController.apply {
            isAppearanceLightStatusBars = !Utils.isDarkMode(context)
            isAppearanceLightNavigationBars = !Utils.isDarkMode(context)
        }

        // Set transparent status and navigation bar colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Use modern API
            setDecorFitsSystemWindows(false)
            insetsController.isAppearanceLightStatusBars = !Utils.isDarkMode(context)
            insetsController.isAppearanceLightNavigationBars = !Utils.isDarkMode(context)
        } else {
            // Backward compatibility for older versions
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }

    private fun updateUI() {
        if (!isAdded) return

        layout.apply {

            val textColor = getAttr(context, android.R.attr.textColor)

            titleView.setTextColor(themeColor)
            messageView.setTextColor(textColor)

            titleView.text = title.takeIf { it.isNotEmpty() } ?: run {
                titleView.visibility = View.GONE
                return@run ""
            }

            messageView.text = message.takeIf { it.isNotEmpty() } ?: run {
                messageView.visibility = View.GONE
                return@run ""
            }

            cancelButton.apply {
                background = ColorDrawable()
                setTextColor(textColor)
                text = dismissText
                visibility = if (isCancelActionButtonVisible) View.VISIBLE else View.GONE
            }

            acceptButton.apply {
                backgroundTintList = ColorStateList.valueOf(themeColor)
                setTextColor(getColorLuminance(themeColor))
                text = acceptText
                visibility = if (isAcceptActionButtonVisible) View.VISIBLE else View.GONE
            }

            if(viewBinding == null) {
                layoutBindHelper?.let { helper ->
                    helper.onInflateCallback?.let { callback ->
                        val binding = helper.inflate?.invoke(
                            null,
                            layoutInflater,
                            null,
                            false
                        ) as? ViewBinding
                        binding?.let { validBinding ->
                            callback(validBinding, this@DialogBottom)
                            frame = validBinding.root
                        }
                    }
                } ?: run {
                    titleView.visibility = View.VISIBLE
                    messageView.visibility = View.VISIBLE
                }
            } else {
                frame = viewBinding!!.root
            }
        }
    }

    class LayoutBindHelper<T : ViewBinding> {
        var inflate: Method? = null
        var onInflateCallback: ((binding: T, dialog: DialogBottom) -> Unit)? = null
    }

    internal interface  DialogBottomProperty {
        var title: String
        var message: String
        var isCancelable: Boolean
        var dismissText: String
        var dismissHandler: ((DialogBottom) -> Boolean)?
        var acceptText: String
        var acceptHandler: ((DialogBottom) -> Boolean)?
        var isCancelActionButtonVisible: Boolean
        var isAcceptActionButtonVisible: Boolean
    }

    class Builder(private val activity: FragmentActivity): DialogBottomProperty {

        var color: Int = getAttr(activity, android.R.attr.textColor)
        override var title: String = ""
        override var message: String = ""
        override var isCancelable: Boolean = true
        override var dismissText: String = "Cancel"
        override var dismissHandler: ((DialogBottom) -> Boolean)? = null
        override var acceptText: String = "Accept"
        override var acceptHandler: ((DialogBottom) -> Boolean)? = null
        override var isCancelActionButtonVisible: Boolean = true
        override var isAcceptActionButtonVisible: Boolean = true

        private var layoutBindHelper: LayoutBindHelper<ViewBinding>? = null

        fun <T : ViewBinding> setLayout(viewBindingClass: Class<T>, callback: (binding: T, dialog: DialogBottom) -> Unit) {
            layoutBindHelper = LayoutBindHelper<T>().apply {
                inflate = viewBindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
                onInflateCallback = callback
            } as LayoutBindHelper<ViewBinding>
        }

        private var viewBinding: ViewBinding? = null
        fun setLayout(viewBinding: ViewBinding) {
            this.viewBinding = viewBinding
        }

        fun build(): DialogBottom {
            val dialog = DialogBottom()
            dialog.themeColor = color
            dialog.title = title
            dialog.message = message
            dialog.isCancelable = isCancelable
            dialog.dismissText = dismissText
            dialog.dismissHandler = dismissHandler
            dialog.acceptText = acceptText
            dialog.acceptHandler = acceptHandler
            dialog.layoutBindHelper = layoutBindHelper
            dialog.viewBinding = viewBinding
            dialog.isCancelActionButtonVisible = isCancelActionButtonVisible
            dialog.isAcceptActionButtonVisible = isAcceptActionButtonVisible
            return dialog
        }

        fun show(): DialogBottom {
            val dialog = build()
            dialog.show(activity.supportFragmentManager, DialogBottom::class.java.simpleName)
            return dialog
        }
    }

    class DialogContainer(context: Context): ViewBinding, LinearLayout(context) {

         val titleView: TextView by lazy {
            TextView(context).apply {
                setPadding(0, 5.dp, 0, 5.dp)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                textSize = 22f
                typeface = resources.getFont(R.font.montserrat_semi_bold)
            }
        }
        val messageView: TextView by lazy {
            TextView(context).apply {
                setPadding(0, 0, 0, 16.dp)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                textSize = 16f
                typeface = resources.getFont(R.font.montserrat_regular)
            }
        }
        val acceptButton: MaterialButton by lazy {
            MaterialButton(context).apply {
                text = "Accept"
                typeface = resources.getFont(R.font.montserrat_bold)

                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(18.dp, 0, 0, 0)
                }
            }
        }
        val cancelButton: MaterialButton by lazy {
            MaterialButton(context).apply {
                text = "Cancel"
                typeface = resources.getFont(R.font.montserrat_bold)

                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(18.dp, 0, 0, 0)
                }
            }
        }
        private val hostFragment: FrameLayout by lazy {
            FrameLayout(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            }
        }
        private val bottomView: LinearLayout by lazy {
            LinearLayout(context).apply {
                orientation = HORIZONTAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                gravity = Gravity.END
                addView(cancelButton)
                addView(acceptButton)
            }
        }

        var frame: View
            get() = hostFragment
            set(value) {
                (value.parent as? ViewGroup)?.removeView(value)
                hostFragment.removeAllViews()
                hostFragment.addView(value)
            }

        init {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setPadding(18.dp, 22.dp, 18.dp, 22.dp)
            }
            addView(titleView)
            addView(messageView)
            addView(hostFragment)
            addView(bottomView)
        }

        // private val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()

        override fun getRoot(): LinearLayout {
            return this
        }
    }
}



