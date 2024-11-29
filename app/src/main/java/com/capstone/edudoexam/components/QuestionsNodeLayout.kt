package com.capstone.edudoexam.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.capstone.edudoexam.R

class QuestionsNodeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var _itemNodeListener: ItemNodeListener? = null
    private var _nodeLength: Int = 0

    /**
     * Listener for node item events.
     */
    var itemNodeListener: ItemNodeListener?
        get() = _itemNodeListener
        set(value) {
            if (_itemNodeListener != value) {
                _itemNodeListener = value
                render()
            }
        }

    /**
     * The number of nodes to display.
     */
    var nodeLength: Int
        get() = _nodeLength
        set(value) {
            if (_nodeLength != value) {
                _nodeLength = value
                render()
            }
        }

    init {
        orientation = VERTICAL
        context.withStyledAttributes(attrs, R.styleable.QuestionsNodeLayout) {
            nodeLength = getInt(R.styleable.QuestionsNodeLayout_questionLength, 0)
        }
        render()
    }

    private fun render() {
        removeAllViews()

        for (i in 0 until nodeLength) {
            if (i % 5 == 0) {
                addRow()
            }
            addItem(i)
        }
    }

    private fun addRow() {
        val rowLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 55.dp).apply {
                setMargins(0, 8.dp, 0, 8.dp)
            }
        }
        addView(rowLayout)
    }

    @SuppressLint("SetTextI18n")
    private fun addItem(index: Int) {
        val lastRow = getChildAt(childCount - 1) as? LinearLayout ?: return

        val itemNode = QuestionNode(context).apply {
            text = "${index + 1}"
            setOnClickListener {
                itemNodeListener?.onItemClick(this, index)
                showInfoWindow()
            }
        }

        // Notify the listener when a node is created
        itemNodeListener?.onNodeCreate(itemNode, index)

        itemNode.layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(8.dp, 0, 8.dp, 0)
        }

        setAspectRatio(itemNode)

        lastRow.addView(itemNode)
    }

    /**
     * Ensures the node has a 1:1 aspect ratio.
     */
    private fun setAspectRatio(view: QuestionNode) {
        view.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    view.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = view.width
                    view.layoutParams.height = width
                    return true
                }
            }
        )
    }

    private val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()

    /**
     * Interface for item node callbacks.
     */
    interface ItemNodeListener {
        /**
         * Called when a node is created.
         * @param node The created `QuestionNode`.
         * @param index The index of the node.
         */
        fun onNodeCreate(node: QuestionNode, index: Int)

        /**
         * Called when a node is clicked.
         * @param node The clicked `QuestionNode`.
         * @param index The index of the node.
         */
        fun onItemClick(node: QuestionNode, index: Int)
    }
}
