package com.il4mb.edudoexam.models

import androidx.recyclerview.widget.DiffUtil
import com.android.billingclient.api.ProductDetails

data class ProductItem(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val benefits: List<String>
) {
    class DiffCallback: DiffUtil.ItemCallback<ProductItem>() {
        override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem, ): Boolean {
            return oldItem.productId == newItem.productId
        }
        override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem, ): Boolean {
            return oldItem == newItem
        }
    }
}