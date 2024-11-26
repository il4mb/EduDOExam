package com.capstone.edudoexam.models

data class Answer(
    val id: String,
    val choice: Char,
    val questionId: String,
    val userId: String
)