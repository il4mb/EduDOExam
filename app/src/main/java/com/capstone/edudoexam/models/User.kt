package com.capstone.edudoexam.models

import android.content.Context

data class User(
    val id: String,
    val name: String,
    val email: String,
    val gender: Int
) {

    companion object {
        const val TOKEN_REF = "auth-token"

        fun getCurrentUser(context: Context): User? {
            val sharedPref = context.getSharedPreferences(TOKEN_REF, Context.MODE_PRIVATE)
            val id = sharedPref.getString("id", null)
            val name = sharedPref.getString("name", null)
            val email = sharedPref.getString("email", null)
            val gender = sharedPref.getInt("gender", -1)
        }
    }
}
