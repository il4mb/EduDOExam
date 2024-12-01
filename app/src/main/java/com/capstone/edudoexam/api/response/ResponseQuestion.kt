package com.capstone.edudoexam.api.response

import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.models.Question

class ResponseQuestion(
    error: Boolean,
    message: String,
    val questions: MutableList<Question>
): Response(error, message)
