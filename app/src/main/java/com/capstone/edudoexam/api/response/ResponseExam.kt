package com.capstone.edudoexam.api.response

import com.capstone.edudoexam.models.Exam

class ResponseExam(
    error: Boolean,
    message: String,
    val exam: Exam
) : Response(error, message)