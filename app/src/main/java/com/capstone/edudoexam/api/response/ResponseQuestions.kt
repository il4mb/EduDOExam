package com.capstone.edudoexam.api.response

import com.capstone.edudoexam.models.Question

class ResponseQuestions(
    error: Boolean,
    message: String,
    val questions: MutableList<Question>
): Response(error, message)


class ResponseQuestion(
    error: Boolean,
    message: String,
    val question: Question
): Response(error, message)