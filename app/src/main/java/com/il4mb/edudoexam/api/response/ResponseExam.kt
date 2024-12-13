package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.Exam

class ResponseExam(
    error: Boolean,
    message: String,
    val exam: Exam
) : Response(error, message)