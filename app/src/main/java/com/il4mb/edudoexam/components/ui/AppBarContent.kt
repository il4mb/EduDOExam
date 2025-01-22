package com.il4mb.edudoexam.components.ui

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.tools.Utils.Companion.dp
import com.il4mb.edudoexam.tools.Utils.Companion.getViewsByType

class AppBarContent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    val container: LinearLayout by lazy {
        LinearLayout(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }
    }

    init {
        addView(container)

        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                val width = view?.width?.toFloat() ?: 0f
                val height = view?.height?.toFloat() ?: 0f
                val radius = 75f
                outline?.setRoundRect(0, -radius.toInt(), width.toInt(), height.toInt(), radius)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val appBarLayout = (rootView as? ViewGroup)?.getViewsByType(AppBarLayout::class.java)?.firstOrNull()
        if(appBarLayout != null) {
            removeView(container)
            appBarLayout.setContentView(container.apply {
                setPadding(0.dp, 0, 0.dp, 0.dp)
            })
            visibility = View.GONE
        } else {
            background = ContextCompat.getDrawable(context, R.drawable.rounded_bottom_corner)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val viewsToMove = mutableListOf<View>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != container) {
                viewsToMove.add(child)
            }
        }
        viewsToMove.forEach { child ->
            removeView(child)
            container.addView(child)
        }

        setTextColor()
    }

    fun setTextColor() {
        container.children.forEach { view ->
            if (view is ViewGroup) {
                setNestedTextColor(view)
            } else if (view is TextView) {
                view.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }

    }

    private fun setNestedTextColor(view: ViewGroup) {
        view.children.forEach { child ->
            if (child is ViewGroup) {
                setNestedTextColor(child)
            } else if (child is TextView) {
                child.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }
}
