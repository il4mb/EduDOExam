package com.capstone.edudoexam.components.ui

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

    // Render the nodes based on nodeLength
    private fun render() {
        removeAllViews()

        for (i in 0 until nodeLength) {
            if (i % 5 == 0) {
                addRow()
            }
            addItem(i)
        }
    }

    // Add a new row to the layout
    private fun addRow() {
        val rowLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 55.dp).apply {
                setMargins(0, 8.dp, 0, 8.dp)
                weightSum = 5f
            }
        }
        addView(rowLayout)
    }

    // Add a new item node to the last row
    @SuppressLint("SetTextI18n")
    private fun addItem(index: Int) {
        val lastRow = getChildAt(childCount - 1) as? LinearLayout ?: return
        val itemNode = QuestionNode(context).apply {
            text = "${index + 1}"
            setOnClickListener {
                itemNodeListener?.onItemClick(this, index)
            }
        }

        itemNodeListener?.onNodeCreate(itemNode, index)
        itemNode.layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(8.dp, 0, 8.dp, 0)
        }

        // setAspectRatio(itemNode)
        lastRow.addView(itemNode)
    }

    // Retrieve a specific node based on its index
    fun getNode(index: Int): QuestionNode {
        val rowAt: Int = index / 5
        val colAt: Int = index % 5

        if (rowAt >= childCount) throw IndexOutOfBoundsException("Row index out of bounds")
        val rowLayout = getChildAt(rowAt) as? LinearLayout
            ?: throw IllegalStateException("Child at row index is not a LinearLayout")

        if (colAt >= rowLayout.childCount) throw IndexOutOfBoundsException("Column index out of bounds")

        return rowLayout.getChildAt(colAt) as? QuestionNode
            ?: throw IllegalStateException("Child at column index is not a QuestionNode")
    }

    // Retrieve all nodes
    fun getNodes(): List<QuestionNode> {
        val nodes: MutableList<QuestionNode> = mutableListOf()
        for (i in 0 until nodeLength) {
            nodes.add(getNode(i))
        }
        return nodes
    }

    // Extension property for converting dp to pixels
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
