package com.capstone.edudoexam.api

import com.capstone.edudoexam.api.payloads.QuestionPayload
import com.capstone.edudoexam.api.payloads.Login
import com.capstone.edudoexam.api.payloads.Register
import com.capstone.edudoexam.api.payloads.UpdateProfile
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.api.response.ResponseExams
import com.capstone.edudoexam.api.response.ResponseLogin
import com.capstone.edudoexam.api.response.ResponseQuestion
import com.capstone.edudoexam.api.response.ResponseUser
import com.capstone.edudoexam.api.response.ResponseUsers
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface AuthEndpoints {

    @POST("auth/register")
    fun register(
        @Body body: Register
    ): Call<Response>


    @POST("auth/login")
    fun login(
        @Body body: Login
    ): Call<ResponseLogin>
}

interface ProfileEndpoints {
    @GET("profile")
    fun getProfile(): Call<ResponseUser>

    @PUT("profile")
    fun updateProfile(
        @Body body: UpdateProfile
    ): Call<ResponseUser>
}

interface ExamsEndpoints {

    @GET("exams/upcoming")
    fun getUpcomingExam(): Call<ResponseExams>

    @GET("exams")
    fun getExams(): Call<ResponseExams>

    @GET("exams/{examId}/questions")
    fun getQuestions(
        @Path("examId") examId: String
    ): Call<ResponseQuestion>

    @POST("exams/{examId}/questions")
    fun addQuestion(
        @Path("examId") examId: String,
        @Body body: QuestionPayload
    ): Call<ResponseQuestion>

    @PUT("exams/{examId}/questions/{questionId}")
    fun updateQuestion(
        @Path("examId") examId: String,
        @Path("questionId") questionId: String,
        @Body body: QuestionPayload
    ): Call<ResponseQuestion>

    @GET("exams/{examId}/students")
    fun getStudents(
        @Path("examId") examId: String
    ): Call<ResponseUsers>

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