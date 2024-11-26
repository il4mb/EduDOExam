package com.capstone.edudoexam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class Exam(
    open val id: String,
    open val startDate: String,
    open val createdAt: String,
    open val createBy: String,
    open val title: String,
    open val subTitle: String
) : Parcelable {

    fun getQuestions() : List<Question> {
        return listOf()
    }

    fun getUsers() : List<User> {
        return listOf()
    }

    fun getOwner(): User {
        return User("","Dummy", "Dummy", 1)
    }
}
