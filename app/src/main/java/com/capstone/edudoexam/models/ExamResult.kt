package com.capstone.edudoexam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExamResult(
    override val id: String,
    override val startDate: String,
    override val createdAt: String,
    override val createBy: String,
    override val title: String,
    override val subTitle: String,
    val score: Float,
    val status: String
) : Exam(id, startDate, createdAt, createBy, title, subTitle), Parcelable {

    fun getAnswers() : List<Answer> {
        return listOf()
    }
}
