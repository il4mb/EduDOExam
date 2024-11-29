package com.capstone.edudoexam.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface AuthEndpoints {

    @POST("register")
    fun register(
        @Body body: RegisterPayload
    ): Call<Response>


    @POST("login")
    fun login(
        @Body body: LoginPayload
    ): Call<ResponseLogin>
}

//interface Endpoints {
//
//    @POST("register")
//    fun register(
//        @Body body: RegisterPayload
//    ): Call<Response>
//
//
//    @POST("login")
//    fun login(
//        @Body body: LoginPayload
//    ): Call<ResponseLogin>
//
//
//    @Multipart
//    @POST("stories")
//    fun addStory(
//        @Header("Authorization") token: String,
//        @Part("description") description: RequestBody,
//        @Part photo: MultipartBody.Part,
//        @Part("lat") lat: RequestBody?,
//        @Part("lon") lon: RequestBody?
//    ): Call<Response>
//
//
//    @GET("stories")
//    fun getAllStories(
//        @Header("Authorization") token : String,
//        @Query("page") page: Int?,
//        @Query("size") size: Int?,
//        @Query("location") location: Int?
//    ): Call<ResponseStories>
//
//
//    @GET("stories/{id}")
//    fun detailStory(
//        @Header("Authorization") token : String,
//        @Path("id") id: Int
//    ): Call<ResponseStory>
//}