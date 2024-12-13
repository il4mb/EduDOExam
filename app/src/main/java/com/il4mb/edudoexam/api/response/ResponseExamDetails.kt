package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.AnswerContainer
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.models.Question
import com.il4mb.edudoexam.models.User

data class ResponseExamDetails(
    val error: Boolean,
    val message: String,
    val exam: Exam,
    val questions: MutableList<Question>,
    val answers: MutableList<AnswerContainer>,
    val users: MutableList<User>
)