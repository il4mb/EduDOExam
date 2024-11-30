package com.capstone.edudoexam.api

import android.content.Context
import androidx.fragment.app.FragmentActivity
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference

class Client {

    companion object {

        private const val BASE_URL = "http://192.168.100.6:5000/api/"


        private var activityReference: WeakReference<FragmentActivity>? = null
        private var retrofitInstance: Retrofit? = null


        fun <T> beginWith(activity: FragmentActivity, endpointClass: Class<T>): T {
            activityReference = WeakReference(activity)

            if (retrofitInstance == null || activityReference?.get() == null) {
                retrofitInstance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildClient(activity))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            return retrofitInstance!!.create(endpointClass)
        }

        private fun buildClient(activity: FragmentActivity): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(activity))
                .build()
        }
    }
}
