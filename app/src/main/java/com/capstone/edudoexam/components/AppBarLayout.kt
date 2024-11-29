package com.capstone.edudoexam.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.google.android.material.appbar.AppBarLayout as AppBarLayoutMaterial

@SuppressLint("ObjectAnimatorBinding")
class AppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppBarLayoutMaterial(context, attrs, defStyleAttr) {

    var title: String
        get() = toolbar.title.toString()
        set(value) {
            toolbar.title = value
        }

    var subtitle: String
        get() = toolbar.subtitle.toString()
        set(value) {
            toolbar.subtitle = value
        }

    val toolbar: Toolbar by lazy {
        Toolbar(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            background = ColorDrawable() // Consider setting a default color if required
        }
    }

    private val menuLayout: MenuLayout by lazy {
        MenuLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }
        }
    }

    private val relativeLayout: RelativeLayout by lazy {
        RelativeLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            addView(toolbar, RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            addView(menuLayout, RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
                setMargins(0, 0, 16.dp, 0)
            })
            background = ColorDrawable()
            elevation = 0f
        }
    }

    private val container: FrameLayout by lazy {
        FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            background = ColorDrawable()
            elevation = 0f
        }
    }

    fun addMenu(@DrawableRes icon: Int, @ColorInt color: Int, onClick: (View) -> Unit) : View? {
        TransitionManager.beginDelayedTransition(menuLayout, ChangeBounds())
        return menuLayout.addMenu(icon, color)?.apply {
            setOnClickListener(onClick)
        }
    }

    fun removeAllMenus() {
        TransitionManager.beginDelayedTransition(menuLayout, ChangeBounds())
        menuLayout.removeAllViews()
    }

    fun addContentView(view: View?) {
        view?.let { newView ->
            newView.alpha = 0.6f
            newView.translationY = -20f

            container.addView(newView)

            newView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start()

            val oldViews = container.children.toList()
            if (oldViews.size > 1) {
                val oldView = oldViews[0]
                animateOut(oldView) {
                    container.removeView(oldView)
                }
            }
        } ?: run {
            removeAllContentViews()
        }
    }

    fun removeAllContentViews(finished: (() -> Unit)? = null) {
        val children = container.children.toList()
        children.forEach { child ->
            animateOut(child) {
                TransitionManager.beginDelayedTransition(container, ChangeBounds())
                container.removeView(child)
                if (child == children.last()) {
                    finished?.invoke()
                }
            }
        }
    }

    private fun animateOut(v: View, onAnimationEnd: () -> Unit) {
        v.animate()
            .setDuration(280)
            .translationY(-20f)
            .alpha(0f)
            .withEndAction {
                onAnimationEnd()
            }
            .start()
    }

    init {

        addView(relativeLayout)
        addView(container)

        // Load custom attributes
        context.withStyledAttributes(attrs, R.styleable.AppBarLayout) {
            title = getString(R.styleable.AppBarLayout_title) ?: ""
            subtitle = getString(R.styleable.AppBarLayout_subtitle) ?: ""
        }
        elevation = 0f
    }
}
