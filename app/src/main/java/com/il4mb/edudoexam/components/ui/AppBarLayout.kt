package com.il4mb.edudoexam.components.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.google.android.material.appbar.AppBarLayout as AppBarLayoutMaterial

@SuppressLint("ObjectAnimatorBinding", "ResourceAsColor")
class AppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppBarLayoutMaterial(context, attrs, defStyleAttr) {

    val toolbar: Toolbar by lazy {
        Toolbar(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setPadding(14.dp, 14.dp, 100.dp, 14.dp)
            }
            background = ColorDrawable()
            elevation = 0f
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
            elevation = 0f
        }
    }

    private val customToolbar: RelativeLayout by lazy {
        RelativeLayout(context).apply {
            layoutParams = CollapsingToolbarLayout.LayoutParams(
                CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, 100.dp
            ).apply {
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
            }

            addView(toolbar, RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            addView(menuLayout, RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
                setMargins(0, 0, 0, 0)
            })
            elevation = 0f
        }
    }

    private val container: FrameLayout by lazy {
        FrameLayout(context).apply {
            layoutParams = CollapsingToolbarLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setPadding(0, 45.dp, 0,0)
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
            }
            elevation = 0f
        }
    }


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

    fun addMenu(@DrawableRes icon: Int, @ColorInt color: Int, onClick: (View) -> Unit) : MenuLayout.MenuItem {
        TransitionManager.beginDelayedTransition(menuLayout, ChangeBounds())
        return menuLayout.addMenu(icon, color).apply {
            setOnClickListener(onClick)
        }
    }
    fun addMenu(menuItem: MenuLayout.MenuItem) {
        menuLayout.addMenu(menuItem)
    }
    fun removeAllMenus() {
        TransitionManager.beginDelayedTransition(menuLayout, ChangeBounds())
        menuLayout.removeAllViews()
    }

    fun addContentView(view: View?) {
        view?.let { newView ->
            newView.alpha = 0.6f
            newView.translationY = -20f
            val parent = view.parent
            if (parent is ViewGroup) {
                parent.removeView(view)
            }
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

        addView(CollapsingToolbarLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                setPadding(1.dp, 1.dp, 1.dp, 1.dp)
                minimumHeight = customToolbar.height
            }
            setContentScrimColor(Color.TRANSPARENT)
            setExpandedTitleColor(android.R.color.transparent)
            setCollapsedTitleTextColor(android.R.color.white)

            addView(customToolbar, CollapsingToolbarLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(0, 15.dp, 0, 15.dp)
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
            })

            addView(container, CollapsingToolbarLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 25.dp
                setPadding(0, 15.dp, 0, 15.dp)
                collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
            })
        })

        minimumHeight = customToolbar.height

        context.withStyledAttributes(attrs, R.styleable.AppBarLayout) {
            title = getString(R.styleable.AppBarLayout_title) ?: ""
            subtitle = getString(R.styleable.AppBarLayout_subtitle) ?: ""
        }
        outlineProvider = null
    }
}
