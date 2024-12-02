package com.capstone.edudoexam.api.payloads

data class QuestionPayload(
    val description: String,
    val image: String?,
    val duration: Double?,
    val correctOption: Char?,
    val options: Map<Char, String>?,
    var order: Int?
)
