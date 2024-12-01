package com.capstone.edudoexam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Exam(
    val id: String,
    val createdBy: String,
    val createdAt: String,
    val startDate: String,
    val title: String,
    val subTitle: String,
    val users: MutableList<String>,
    val isOwner: Boolean
) : Parcelable {

    fun getQuestions() : List<Question> {
        return listOf()
    }

    fun getOwner(): User {
        return User("","Dummy", "Dummy", 1)
    }

    val startDateAsLocalDate: LocalDateTime get() {
        return LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME)
    }
    val simplifiedStartDate: String get() {
        return "${startDateAsLocalDate.dayOfMonth} ${startDateAsLocalDate.month} ${startDateAsLocalDate.year} at ${startDateAsLocalDate.hour}:${startDateAsLocalDate.minute}"
    }

    val createdAtAsLocalDate: LocalDateTime get() {
        return LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
    }
    val simplifiedCreatedAt: String get() {
        return "${createdAtAsLocalDate.dayOfMonth} ${createdAtAsLocalDate.month} ${createdAtAsLocalDate.year} at ${createdAtAsLocalDate.hour}:${createdAtAsLocalDate.minute}"
    }
}
