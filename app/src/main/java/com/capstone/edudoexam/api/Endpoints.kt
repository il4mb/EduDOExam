package com.capstone.edudoexam.api

import com.capstone.edudoexam.api.payloads.AddStudentPayload
import com.capstone.edudoexam.api.payloads.AnswersPayload
import com.capstone.edudoexam.api.payloads.ExamPayload
import com.capstone.edudoexam.api.payloads.Login
import com.capstone.edudoexam.api.payloads.QuestionsOrderPayload
import com.capstone.edudoexam.api.payloads.Register
import com.capstone.edudoexam.api.payloads.UpdateProfile
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.api.response.ResponseExam
import com.capstone.edudoexam.api.response.ResponseStudentExamResult
import com.capstone.edudoexam.api.response.ResponseExams
import com.capstone.edudoexam.api.response.ResponseLogin
import com.capstone.edudoexam.api.response.ResponseQuestion
import com.capstone.edudoexam.api.response.ResponseQuestions
import com.capstone.edudoexam.api.response.ResponseTeacherExamResult
import com.capstone.edudoexam.api.response.ResponseUser
import com.capstone.edudoexam.api.response.ResponseUsers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


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

    @GET("exams/ongoing")
    fun getOngoingExams(): Call<ResponseExams>

    @GET("exams/finished")
    fun getFinished(): Call<ResponseExams>

    @GET("exams")
    fun getExams(): Call<ResponseExams>

    @POST("exams")
    fun addExam(
        @Body body: ExamPayload
    ): Call<Response>

    @GET("exams/{examId}")
    fun getExam(
        @Path("examId") examId: String
    ): Call<ResponseExam>

    @PUT("exams/{examId}")
    fun updateExam(
        @Path("examId") examId: String,
        @Body body: ExamPayload
    ): Call<Response>

    @POST("exams/{examId}/join")
    fun joinExam(
        @Path("examId") examId: String
    ): Call<Response>

    @GET("exams/{examId}/student/result")
    fun getExamResultForStudent(
        @Path("examId") examId: String
    ): Call<ResponseStudentExamResult>

    @GET("exams/{examId}/teacher/result")
    fun getExamResultForTeacher(
        @Path("examId") examId: String
    ): Call<ResponseTeacherExamResult>

    @GET("exams/{examId}/questions")
    fun getQuestions(
        @Path("examId") examId: String
    ): Call<ResponseQuestions>

    @Multipart
    @POST("exams/{examId}/questions")
    fun addQuestion(
        @Path("examId") examId: String,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("duration") duration: RequestBody,
        @Part("correctOption") correctOption: RequestBody,
        @Part("options") options: RequestBody
    ): Call<ResponseQuestion>

    @PUT("exams/{examId}/questions/order")
    fun saveQuestionOrder(
        @Path("examId") examId: String,
        @Body body: QuestionsOrderPayload
    ): Call<Response>

    @Multipart
    @PUT("exams/{examId}/questions/{questionId}")
    fun updateQuestion(
        @Path("examId") examId: String,
        @Path("questionId") questionId: String,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("duration") duration: RequestBody,
        @Part("correctOption") correctOption: RequestBody,
        @Part("options") options: RequestBody,
        @Part("order") order: RequestBody
    ): Call<ResponseQuestions>

    @PUT("exams/{examId}/questions/{questionId}/{toPosition}")
    fun moveQuestion(
        @Path("examId") examId: String,
        @Path("questionId") questionId: String,
        @Path("toPosition") toPosition: Int,
    ): Call<Response>

    @DELETE("exams/{examId}/questions/{questionId}")
    fun removeQuestion(@Path("examId") examId: String, @Path("questionId") questionId: String): Call<Response>

    @GET("exams/{examId}/students")
    fun getStudents(
        @Path("examId") examId: String,
        @Query("block") block: Boolean
    ): Call<ResponseUsers>

    @POST("exams/{examId}/students")
    fun addStudent(
        @Path("examId") examId: String,
        @Body body: AddStudentPayload
    ): Call<Response>

    @DELETE("exams/{examId}/students/{userId}")
    fun removeStudent(
        @Path("examId") examId: String,
        @Path("userId") userId: String
    ): Call<Response>

    @PUT("exams/{examId}/students/{userId}")
    fun updateStudent(
        @Path("examId") examId: String,
        @Path("userId") userId: String,
        @Query("block") block: Boolean
    ): Call<Response>

    @POST("exams/{examId}/answers")
    fun addAnswer(
        @Path("examId") examId: String,
        @Body body: AnswersPayload
    ): Call<Response>

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