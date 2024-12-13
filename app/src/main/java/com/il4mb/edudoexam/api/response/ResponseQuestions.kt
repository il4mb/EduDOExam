package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.Question

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