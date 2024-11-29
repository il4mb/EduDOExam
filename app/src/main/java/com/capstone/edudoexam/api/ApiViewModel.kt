package com.capstone.edudoexam.api

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

abstract class ApiViewModel<T: Any>: ViewModel(), Callback<T> {

    abstract fun onSuccess(response: T)
    abstract fun onError(error: Response)

    override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
        if(response.isSuccessful) {
            response.body()?.let {
                onSuccess(it)
            }
        } else {
            val errorBody = response.errorBody()?.string()
            try {
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                onError(errorResponse)

            } catch (e: Exception) {
                Log.e("API_ERROR", "Failed to parse error body: $errorBody", e)
                onError(Response(true, "Failed to parse error body: $errorBody"))
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        onError(Response(true, "Error: ${t.message}"))
    }
}