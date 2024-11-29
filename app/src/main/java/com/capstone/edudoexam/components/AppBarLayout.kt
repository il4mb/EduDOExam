package com.capstone.edudoexam.components

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.postDelayed
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.google.android.material.appbar.AppBarLayout as AppBarLayoutMaterial

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
        }
    }

    private val container: LinearLayout by lazy {
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
    }

    fun addMenu(@DrawableRes icon: Int, @ColorInt color: Int, onClick: (View) -> Unit) : View? {
        return menuLayout.addMenu(icon, color)?.apply {
            setOnClickListener(onClick)
        }
    }

    fun addMenu(@DrawableRes icon: Int, onClick: (View) -> Unit) : View?{
        return menuLayout.addMenu(icon)?.apply {
            setOnClickListener(onClick)
        }
    }

    fun removeAllMenus() {
        menuLayout.removeAllViews()
    }

    fun addContentView(view: View) {
        view.apply {
            alpha = 0f
            translationY = -20f
        }
        container.addView(view.apply {
            animateIn(view)
        })
    }

    fun removeAllContentViews() {
        val children = container.children.toList()
        children.forEachIndexed { index, child ->
            child.postDelayed({
                animateOut(child) { container.removeView(child) }
            }, index * 50L)
        }
    }

    private fun animateIn(v: View) {
        v.apply {
            translationY = -10f
            alpha = 0f
            animate()
                .setDuration(200)
                .translationY(0f)
                .alpha(1f)
                .start()
        }
    }
    private fun animateOut(v: View, onAnimationEnd: () -> Unit) {
        v.animate()
            .setDuration(200)
            .alpha(0f)
            .translationY(-10f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { onAnimationEnd() }
            .start()
    }

    init {
        // Add toolbar and container to the AppBarLayout
        addView(relativeLayout)
        addView(container)

        // Load custom attributes
        context.withStyledAttributes(attrs, R.styleable.AppBarLayout) {
            title = getString(R.styleable.AppBarLayout_title) ?: ""
            subtitle = getString(R.styleable.AppBarLayout_subtitle) ?: ""
        }
    }
}
