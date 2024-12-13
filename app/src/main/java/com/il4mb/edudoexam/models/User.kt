package com.il4mb.edudoexam.models

import android.content.Context
import androidx.preference.PreferenceManager

data class User(
    val id: String,
    val name: String,
    val email: String,
    val gender: Int,
    val photo: String?,
    val quota: Int = 0,
    val currentPackage: AccountPackage?
) {

    companion object {
        const val TOKEN_REF = "auth-token"

        fun getAuthToken(context: Context): String? {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPref.getString(TOKEN_REF, null)
        }
        fun setAuthToken(context: Context, token: String) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPref.edit()) {
                putString(TOKEN_REF, token)
                commit()
            }
        }
    }
}
