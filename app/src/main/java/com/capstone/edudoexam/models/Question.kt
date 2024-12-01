package com.capstone.edudoexam.models

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Question(
    val id: String,
    val examId: String,
    val description: String,
    val image: String?,
    val duration: Double,
    val correctOption: Char,
    var order: Int,
    val options: Map<Char, String>
): Parcelable {


    val Double.asDurationFormatted: String
        @SuppressLint("DefaultLocale")
        get() {
            val minutes = this.toInt() // Get the integer part of the number (minutes)
            val seconds = ((this - minutes) * 60).toInt() // Calculate the remaining seconds
            return String.format("%02d:%02d", minutes, seconds)
        }


    val Map<Char, String>.asQuestionOptions: QuestionOptions
        get() = QuestionOptions(
            a = this['A'] ?: "",
            b = this['B'] ?: "",
            c = this['C'] ?: "",
            d = this['D'] ?: ""
        )

}
