package com.il4mb.edudoexam.api

import androidx.fragment.app.FragmentActivity
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.api.response.ResponseError
import com.il4mb.edudoexam.ui.LoadingHandler
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import com.il4mb.edudoexam.BuildConfig


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
        (activity as? LoadingHandler)?.setLoading(true)
        onCreate(beginWith(activity, clazz)).enqueue(this)
    }

    private fun invokeSuccessCallback(response: R) {
        activity.runOnUiThread {
            onSuccessCallback.forEach {
                it.invoke(response)
            }
        }
    }

    private fun invokeErrorCallback(error: ResponseError) {
        activity.runOnUiThread {
            onErrorCallback.forEach {
                it.invoke(error)
            }
        }
    }

    override fun onResponse(call: Call<R>, response: retrofit2.Response<R>) {
        (activity as? LoadingHandler)?.setLoading(false)
        if(response.isSuccessful) {
            response.body()?.let { res ->
                invokeSuccessCallback(res)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            try {
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                invokeErrorCallback(ResponseError.from(response.code(), errorResponse))
            } catch (e: Exception) {
                invokeErrorCallback(ResponseError(true, e.message ?: "Unknown Error", response.code()))
            }
        }
    }

    override fun onFailure(call: Call<R>, t: Throwable) {
        (activity as? LoadingHandler)?.setLoading(false)
        onErrorCallback.forEach {
            it.invoke(ResponseError(true, t.message ?: "Unknown Error", -1))
        }
    }

    companion object {

        private const val BASE_URL = BuildConfig.BASE_URL
        // private const val BASE_URL = "http://192.168.100.6:8081/api/"
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


