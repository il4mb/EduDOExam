package com.capstone.edudoexam.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * @param T the item class
 * @param VB the ViewBinding inflation
 */
open class GenericListAdapter<T, VB : ViewBinding>(
    private val inflateBinding: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val onItemBindCallback: ItemBindListener<T, VB>,
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, GenericListAdapter.CustomViewHolder<VB>>(diffCallback)
{

    override fun onBindViewHolder(holder: CustomViewHolder<VB>, position: Int) {
        val item = getItem(position)
        onItemBindCallback.onViewBind(holder.binding, item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder<VB> {
        val binding = inflateBinding(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    class CustomViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    /**
     * <T> Item
     */
    interface ItemBindListener<T, VB : ViewBinding> {
        fun onViewBind(binding: VB, item: T)
    }
}
