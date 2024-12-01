package com.capstone.edudoexam.api

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.capstone.edudoexam.api.response.Response
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

    private var onSuccessCallback: ((R) -> Unit)? = null
    private var onErrorCallback: ((error: Response) -> Unit)? = null

    fun onSuccess(callback: (R) -> Unit): Client<T, R> {

        onSuccessCallback = callback
        return this
    }

    fun onError(callback: (error: Response) -> Unit): Client<T, R> {
        onErrorCallback = callback
        return this
    }

    fun fetch(onCreate: (endpoint: T) -> Call<R>) {
        onCreate(beginWith(activity, clazz)).enqueue(this)
    }

    override fun onResponse(call: Call<R>, response: retrofit2.Response<R>) {
        if(response.isSuccessful) {
            response.body()?.let {
                onSuccessCallback?.invoke(it)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            try {
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                onErrorCallback?.invoke(errorResponse)

            } catch (e: Exception) {
                Log.e("API_ERROR", "Failed to parse error body: $errorBody", e)
                onErrorCallback?.invoke(Response(true, "Failed to parse error body: $errorBody"))
            }
        }
    }

    override fun onFailure(call: Call<R>, t: Throwable) {
        onErrorCallback?.invoke(Response(true, "Error: ${t.message}"))
    }

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
                // .addInterceptor(ErrorInterceptor())
                .build()
        }
    }

}


