package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.Exam

class ResponseExams(
    error: Boolean,
    message: String,
    val exams: MutableList<Exam>
) : Response(error, message)