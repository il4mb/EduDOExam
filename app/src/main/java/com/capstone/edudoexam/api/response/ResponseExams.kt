package com.capstone.edudoexam.api.response

import com.capstone.edudoexam.models.Exam

class ResponseExams(
    error: Boolean,
    message: String,
    val exams: MutableList<Exam>
) : Response(error, message)