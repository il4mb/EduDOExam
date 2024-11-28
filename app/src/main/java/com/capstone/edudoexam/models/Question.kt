package com.capstone.edudoexam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Question(
    val id: String,
    val examId: String,
    val description: String,
    val duration: Int,
    val correct: Char,
    var order: Int,
    val options: Map<Char, String>
): Parcelable
