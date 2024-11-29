package com.capstone.edudoexam.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.core.view.get
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.google.android.material.card.MaterialCardView

class MenuLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val recycledViews = mutableListOf<MaterialCardView>()

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        orientation = HORIZONTAL
    }

    fun addMenu(@DrawableRes iconRes: Int, @ColorInt tint: Int = 0): View? {
        return ContextCompat.getDrawable(context, iconRes)?.let {
            if (tint != 0) {
                val icon = it.mutate()
                DrawableCompat.setTint(icon, tint)
                addMenu(icon)
            } else {
                addMenu(it)
            }
        }
    }

    fun addMenu(icon: Drawable): View {
        val menuItem = if (recycledViews.isNotEmpty()) {
            recycledViews.removeAt(0).apply {
                resetViewProperties(this)
                (this[0] as ImageView).setImageDrawable(icon)
            }
        } else {
            createMenuItem(icon)
        }
        return menuItem.apply {
            this@MenuLayout.addView(this)
            animateIn(this)
        }
    }

    private fun removeMenus() {
        val viewsToRemove = children.toList()
        viewsToRemove.forEachIndexed { index, child ->
            child.postDelayed({
                animateOut(child) {
                    recycledViews.add(child as MaterialCardView)
                    removeView(child)
                }
            }, index * 50L)
        }
    }

    private fun animateIn(view: View) {
        view.apply {
            translationY = 10f
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .setDuration(200)
                .translationY(0f)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun animateOut(view: View, onAnimationEnd: () -> Unit) {
        view.animate()
            .setDuration(200)
            .alpha(0f)
            .translationY(10f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                onAnimationEnd()
            }
            .start()
    }

    private fun resetViewProperties(view: MaterialCardView) {
        view.apply {
            alpha = 1f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
        }
    }

    private fun createMenuItem(icon: Drawable): MaterialCardView {
        val cardView = MaterialCardView(context).apply {
            radius = 360f
            strokeColor = Color.TRANSPARENT
            layoutParams = LayoutParams(
                40.dp,
                40.dp
            ).apply {
                marginEnd = 8.dp
            }
            foreground = ContextCompat.getDrawable(context, R.drawable.ripple_effect)
        }
        val imageView = ImageView(context).apply {
            setImageDrawable(icon)
        }
        cardView.addView(imageView)
        return cardView
    }

    override fun removeAllViews() {
        removeMenus()
    }
}
