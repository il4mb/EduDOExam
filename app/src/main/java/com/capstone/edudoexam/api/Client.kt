package com.capstone.edudoexam.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Client {

    companion object {
        const val BASE_URL = "http://192.168.100.6:5000/api/"
        fun <T> beginWith(endpointClass: Class<T>) : T
        {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service: T = retrofit.create(endpointClass)
            return service
        }
    }
}