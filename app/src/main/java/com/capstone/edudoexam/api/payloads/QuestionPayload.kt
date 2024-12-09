package com.capstone.edudoexam.api.payloads

import java.io.File

data class QuestionPayload(
    val description: String,
    val image: File?,
    val duration: Double?,
    val correctOption: Char?,
    val options: Map<Char, String>?,
    var order: Int?
)
