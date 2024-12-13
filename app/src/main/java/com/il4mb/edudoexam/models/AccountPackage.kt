package com.il4mb.edudoexam.models

import androidx.recyclerview.widget.DiffUtil

data class AccountPackage(
    val id: String,
    val label: String,
    val maxParticipant: Int = 0,
    val maxQuestion: Int = 0,
    val freeQuota: Int = 0,
    val price: Long = 0
) {

    class DiffCallback : DiffUtil.ItemCallback<AccountPackage>() {
        override fun areItemsTheSame(oldItem: AccountPackage, newItem: AccountPackage): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areContentsTheSame(oldItem: AccountPackage, newItem: AccountPackage): Boolean {
            return oldItem == newItem
        }
    }
}
