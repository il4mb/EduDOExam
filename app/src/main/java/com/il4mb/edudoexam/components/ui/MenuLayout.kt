package com.il4mb.edudoexam.components.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.Utils.Companion.getAttr
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel

class MenuLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    class MenuItem(context: Context) : CardView(context) {

        private val _imageView: ShapeableImageView by lazy {
            ShapeableImageView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                scaleType = ImageView.ScaleType.CENTER_CROP
                shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCornerSizes(50f)
                    .build()
            }
        }

        init {
            radius = 50f
            layoutParams = LayoutParams(40.dp, 40.dp).apply {
                marginEnd = 8.dp
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(getAttr(context, android.R.attr.colorBackground))
            }
            foreground = ContextCompat.getDrawable(context, R.drawable.ripple_effect)
            setPadding(6.dp)

            addView(_imageView)
        }

        fun setImageDrawable(@DrawableRes drawable: Int) {
            _imageView.setImageDrawable(ContextCompat.getDrawable(context, drawable))
        }

        fun setImageDrawable(drawable: Drawable?) {
            _imageView.setImageDrawable(drawable)
        }

        fun setColorFilter(@ColorInt color: Int) {
            _imageView.setColorFilter(color)
        }

        fun setPadding(padding: Int) {
            _imageView.setPadding(padding)
        }
    }

    init {
        orientation = HORIZONTAL
    }

    fun addMenu(@DrawableRes iconRes: Int, @ColorInt tint: Int = 0): MenuItem {
        val menuItem = MenuItem(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, iconRes))
            setColorFilter(tint)
        }
        addView(menuItem)
        animateIn(menuItem)
        return menuItem
    }

    fun addMenu(icon: Drawable): MenuItem {
        val menuItem = MenuItem(context).apply {
            setImageDrawable(icon)
        }
        addView(menuItem)
        animateIn(menuItem)
        return menuItem
    }

    fun addMenu(menuItem: MenuItem) {
        addView(menuItem)
        animateIn(menuItem)
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
}
