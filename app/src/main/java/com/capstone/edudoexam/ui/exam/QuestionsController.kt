package com.capstone.edudoexam.ui.exam

import android.graphics.Color
import com.capstone.edudoexam.components.ui.QuestionNode
import com.capstone.edudoexam.components.ui.QuestionsNodeLayout

class QuestionsController(
    private val questionLayout: QuestionsNodeLayout
): QuestionsNodeLayout.ItemNodeListener {

    private val initialNodeColor = Color.parseColor("#BDBDBD")

    init {
        questionLayout.itemNodeListener =  this
        updateUi()
    }

    var currentPosition: Int = 1
        set(value) {
            field = value
            updateUi()
        }

    private fun updateUi() {
        questionLayout.getNodes().forEachIndexed { index, node ->
            if(index < currentPosition) {
                node.mainColor = Color.parseColor("#FFBC70")
            } else if(index == currentPosition) {
                node.mainColor = Color.parseColor("#FA8500")
            } else {
                node.mainColor = initialNodeColor
            }
        }
    }

    override fun onNodeCreate(node: QuestionNode, index: Int) {

    }

    override fun onItemClick(node: QuestionNode, index: Int) {
        // currentPosition = index
    }

}