package com.capstone.edudoexam.api.payloads

data class QuestionPayload(
    val description: String,
    val image: String?,
    val duration: Double?,
    val correct: Char?,
    val options: Map<Char, String>?,
    var order: Int?
)
