package com.il4mb.edudoexam.models

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



}