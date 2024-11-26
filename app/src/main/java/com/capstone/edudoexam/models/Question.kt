package com.capstone.edudoexam.models

data class Question(
    val id: String,
    val examId: String,
    val description: String,
    val duration: Int,
    val correct: Char,
    val order: Int,
    val options: Map<Char, String>
)
