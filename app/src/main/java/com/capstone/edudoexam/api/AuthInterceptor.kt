package com.capstone.edudoexam.api

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.capstone.edudoexam.ui.welcome.WelcomeActivity
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val fragment: FragmentActivity) : Interceptor {

    companion object {

        private const val USER_TOKEN_KEY = "user_token_session"

        fun saveToken(activity: FragmentActivity, token: String) {
            activity.baseContext.getSharedPreferences(USER_TOKEN_KEY, Context.MODE_PRIVATE)
                .edit()
                .putString(USER_TOKEN_KEY, token)
                .apply()
        }

        fun getToken(activity: FragmentActivity): String? {
            return activity.baseContext.getSharedPreferences(USER_TOKEN_KEY, Context.MODE_PRIVATE)
                .getString(USER_TOKEN_KEY, null)
        }

        fun clearToken(activity: FragmentActivity) {
            activity.baseContext.getSharedPreferences(USER_TOKEN_KEY, Context.MODE_PRIVATE)
                .edit()
                .remove(USER_TOKEN_KEY)
                .apply()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val token = getToken(fragment)

        val authenticatedRequest = if (token != null) {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            request
        }

        val response = chain.proceed(authenticatedRequest)

        if (response.code == 401) {

            clearToken(fragment)
            if(fragment is WelcomeActivity) {
                fragment.startActivity(Intent(fragment, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }

        return response
    }
}