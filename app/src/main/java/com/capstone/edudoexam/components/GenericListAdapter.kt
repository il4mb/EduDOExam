package com.capstone.edudoexam.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.capstone.edudoexam.components.GenericListAdapter.ItemBindListener
import java.lang.reflect.Method

/**
 * @param T the item class
 * @param V the ViewBinding java.class
 * @param viewBindingClass the ViewBinding java.class
 * @param onItemBindCallback the item binding listener
 * @param diffCallback the DiffUtil.ItemCallback
 */
open class GenericListAdapter<T: Any, V : ViewBinding>(
    private val viewBindingClass: Class<V>,
    private val onItemBindCallback: ItemBindListener<T, V>,
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, GenericListAdapter.GenericViewHolder<V>>(diffCallback) {

    override fun onBindViewHolder(holder: GenericViewHolder<V>, position: Int) {
        val item = getItem(position)
        onItemBindCallback.onViewBind(holder.binding, item, position)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<V> {
        val binding = getInflateBinding()(null, LayoutInflater.from(parent.context), parent, false) as V
        return GenericViewHolder(binding)
    }


    class GenericViewHolder< V : ViewBinding>(
        val binding: V
    ) : RecyclerView.ViewHolder(binding.root)

    private fun getInflateBinding(): Method {
        return viewBindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
    }

    // Interface for item binding
    interface ItemBindListener<T, VB : ViewBinding> {
        fun onViewBind(binding: VB, item: T, position: Int)
    }
}
