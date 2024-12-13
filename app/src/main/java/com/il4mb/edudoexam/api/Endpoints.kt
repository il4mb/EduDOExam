package com.il4mb.edudoexam.api

import com.il4mb.edudoexam.api.payloads.AddStudentPayload
import com.il4mb.edudoexam.api.payloads.AnswersPayload
import com.il4mb.edudoexam.api.payloads.ExamPayload
import com.il4mb.edudoexam.api.payloads.Login
import com.il4mb.edudoexam.api.payloads.QuestionsOrderPayload
import com.il4mb.edudoexam.api.payloads.Register
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.api.response.ResponseExam
import com.il4mb.edudoexam.api.response.ResponseStudentExamResult
import com.il4mb.edudoexam.api.response.ResponseExams
import com.il4mb.edudoexam.api.response.ResponseLogin
import com.il4mb.edudoexam.api.response.ResponseStudentAnswer
import com.il4mb.edudoexam.api.response.ResponseParticipants
import com.il4mb.edudoexam.api.response.ResponseQuestion
import com.il4mb.edudoexam.api.response.ResponseQuestions
import com.il4mb.edudoexam.api.response.ResponseTeacherExamResult
import com.il4mb.edudoexam.api.response.ResponseUser
import com.il4mb.edudoexam.api.response.ResponseUsers
import com.il4mb.edudoexam.models.AccountPackage
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

    @Multipart
    @PUT("profile")
    fun updateProfile(
        @Part("name") name: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part photo: MultipartBody.Part?
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

    @DELETE("exams/{examId}")
    fun deleteExam(
        @Path("examId") examId: String
    ): Call<Response>

    @PUT("exams/{examId}")
    fun updateExam(
        @Path("examId") examId: String,
        @Body body: ExamPayload
    ): Call<Response>

    @PUT("exams/{examId}/{action}")
    fun toggleExam(
        @Path("examId") examId: String,
        @Path("action") action: String
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

    @DELETE("exams/{examId}/questions/{questionId}")
    fun removeQuestion(@Path("examId") examId: String, @Path("questionId") questionId: String): Call<Response>

    @GET("exams/{examId}/participants")
    fun getParticipants(
        @Path("examId") examId: String,
        @Query("block") block: Boolean = false
    ): Call<ResponseParticipants>

    @GET("exams/{examId}/answers")
    fun getStudentAnswer(
        @Path("examId") examId: String
    ): Call<ResponseStudentAnswer>


    @GET("exams/{examId}/students")
    fun getStudents(
        @Path("examId") examId: String,
        @Query("block") block: Boolean
    ): Call<ResponseUsers>

    @POST("exams/{examId}/participants")
    fun addParticipant(
        @Path("examId") examId: String,
        @Body body: AddStudentPayload
    ): Call<Response>

    @DELETE("exams/{examId}/participants/{userId}")
    fun removeParticipant(
        @Path("examId") examId: String,
        @Path("userId") userId: String
    ): Call<Response>

    @PUT("exams/{examId}/participants/{userId}")
    fun updateParticipant(
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




data class Pricing(
    val quota: Int
)
data class PriceList(
    val packages:  MutableList<AccountPackage>,
    val pricing: Pricing
)
data class ResponsePriceList(
    val data: PriceList,
    val error: Boolean,
    val message: String
)

data class BuyPayload(
    val packageId: String?,
    val quota: Int?
)


interface ProductEndpoints {

    // Fetch product price list
    @GET("product/price-list")
    fun getProducts(): Call<ResponsePriceList>

    // Process a purchase
    @PUT("product/{userId}")
    fun processPurchase(
        @Path("userId") userId: String,
        @Body body: BuyPayload
    ): Call<Response>
}
