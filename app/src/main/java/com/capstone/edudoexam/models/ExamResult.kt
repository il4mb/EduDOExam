package com.capstone.edudoexam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.security.Timestamp

@Parcelize
data class ExamResult(
    val id: String,
    val examId: String,
    val score: Float,
    val status: String,
) : Parcelable {

    fun getAnswers() : List<Answer> {
        return listOf()
    }
}
