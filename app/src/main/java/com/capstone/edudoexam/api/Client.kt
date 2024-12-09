package com.capstone.edudoexam.api

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.api.response.ResponseError
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference


/**
 * @param T is the endpoint interface
 * @param R is the response body
 */
class Client<T, R>(private val activity: FragmentActivity, private val clazz: Class<T>) : Callback<R> {

    private var onSuccessCallback: MutableList<((R) -> Unit)> = mutableListOf()
    private var onErrorCallback: MutableList<((error: ResponseError) -> Unit)> = mutableListOf()

    fun onSuccess(callback: (R) -> Unit): Client<T, R> {

        onSuccessCallback.add(callback)
        return this
    }

    fun onError(callback: (error: ResponseError) -> Unit): Client<T, R> {
        onErrorCallback.add(callback)
        return this
    }

    fun fetch(onCreate: (endpoint: T) -> Call<R>) {
        onCreate(beginWith(activity, clazz)).enqueue(this)
    }

    override fun onResponse(call: Call<R>, response: retrofit2.Response<R>) {
        if(response.isSuccessful) {
            response.body()?.let { res ->
                onSuccessCallback.forEach{
                    it.invoke(res)
                }
            }
        } else {
            val errorBody = response.errorBody()?.string()
            try {
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                onErrorCallback.forEach {
                    it.invoke(ResponseError.from(response.code(), errorResponse))
                }
            } catch (e: Exception) {
                onErrorCallback.forEach{
                    it.invoke(ResponseError(true, e.message ?: "Unknown Error", response.code()))
                }
            }
        }
    }

    override fun onFailure(call: Call<R>, t: Throwable) {
        onErrorCallback.forEach {
            it.invoke(ResponseError(true, t.message ?: "Unknown Error", -1))
        }
    }

    companion object {

        private const val BASE_URL = "https://capstone-project-441907.et.r.appspot.com/api/"
        private var activityReference: WeakReference<FragmentActivity>? = null
        private var retrofitInstance: Retrofit? = null

        fun <T> beginWith(activity: FragmentActivity, endpointClass: Class<T>): T {
            val currentActivity = activityReference?.get()

            if (retrofitInstance == null || currentActivity == null || currentActivity.isFinishing) {

                activityReference = WeakReference(activity)

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


