package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.AnswerContainer
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.models.Question

class ResponseStudentAnswer(
    error: Boolean,
    message: String,
    val answer: AnswerContainer,
    val exam: Exam,
    val questions: MutableList<Question>
): Response(error, message)