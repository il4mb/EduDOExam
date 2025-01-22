package com.il4mb.edudoexam.components.layout

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.il4mb.edudoexam.R


open class AutoWrapLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    var horizontalGap: Int = 5
    private var verticalGap: Int = 5

    init {
        // Read custom attributes if needed (can be extended to fetch values from XML)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AutoWrapLayout)
            horizontalGap = typedArray.getDimensionPixelSize(R.styleable.AutoWrapLayout_horizontalGap, 0)
            verticalGap = typedArray.getDimensionPixelSize(R.styleable.AutoWrapLayout_verticalGap, 0)
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width = 0
        var height = 0
        var rowWidth = 0
        var rowHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                val lp = child.layoutParams as MarginLayoutParams
                val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
                val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

                if (rowWidth + childWidth + horizontalGap > widthSize) {
                    // Wrap to next row
                    width = maxOf(width, rowWidth)
                    height += rowHeight + verticalGap
                    rowWidth = childWidth
                    rowHeight = childHeight
                } else {
                    rowWidth += childWidth + horizontalGap
                    rowHeight = maxOf(rowHeight, childHeight)
                }
            }
        }

        width = maxOf(width, rowWidth)
        height += rowHeight

        setMeasuredDimension(
            if (widthMode == MeasureSpec.EXACTLY) widthSize else width,
            if (heightMode == MeasureSpec.EXACTLY) heightSize else height
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childRight = measuredWidth - paddingRight

        var curLeft = childLeft
        var curTop = childTop
        var maxHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as MarginLayoutParams
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val childMargins = lp.leftMargin + lp.rightMargin

                if (curLeft + childWidth + childMargins > childRight) {
                    curLeft = childLeft
                    curTop += maxHeight + verticalGap
                    maxHeight = 0
                }

                child.layout(
                    curLeft + lp.leftMargin,
                    curTop + lp.topMargin,
                    curLeft + lp.leftMargin + childWidth,
                    curTop + lp.topMargin + childHeight
                )

                curLeft += childWidth + childMargins + horizontalGap
                maxHeight = maxOf(maxHeight, childHeight + lp.topMargin + lp.bottomMargin)
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }
}

