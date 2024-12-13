package com.il4mb.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.QuestionDiffCallback
import com.il4mb.edudoexam.databinding.ViewItemQuestionBinding
import com.il4mb.edudoexam.models.Question

class QuestionListAdapter(
    onViewBind: ItemBindListener<Question, ViewItemQuestionBinding>
): GenericListAdapter<Question, ViewItemQuestionBinding>(
    viewBindingClass   = ViewItemQuestionBinding::class.java,
    onItemBindCallback = onViewBind,
    diffCallback       = QuestionDiffCallback()
) {

    private var onItemMovedListener: (() -> Unit)? = null

    val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                return false
            }

            val mutableList = currentList.toMutableList()
            val item = mutableList.removeAt(fromPosition)

            mutableList.add(toPosition, item)
            val updatedList = mutableList.mapIndexed { index, question ->
                question.copy(order = index + 1)
            }

            submitList(updatedList)
            notifyItemMoved(fromPosition, toPosition)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            onItemMovedListener?.invoke()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun moveItem(fromOrder: Int, toOrder: Int) {
        val fromPosition = currentList.indexOfFirst { it.order == fromOrder }
        val toPosition = currentList.indexOfFirst { it.order == toOrder }

        if (fromPosition != -1 && toPosition != -1) {
            val mutableList = currentList.toMutableList()

            // Remove the item from the list
            val item = mutableList.removeAt(fromPosition)

            // Insert the item at the new position
            mutableList.add(toPosition, item)

            // Recalculate and update the orders for all items to prevent duplicates
            val updatedList = mutableList.mapIndexed { index, question ->
                question.copy(order = index + 1)  // Ensure unique order for all items
            }

            // Submit the updated list and notify changes
            submitList(updatedList)
            notifyItemMoved(fromPosition, toPosition)
            notifyItemChanged(fromPosition)
            notifyItemChanged(toPosition)

            onItemMovedListener?.invoke()
        }
    }

    fun setOnItemMovedListener(listener: () -> Unit) {
        onItemMovedListener = listener
    }
}


