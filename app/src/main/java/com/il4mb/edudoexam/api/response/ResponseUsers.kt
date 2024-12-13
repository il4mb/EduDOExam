package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.Answer
import com.il4mb.edudoexam.models.User

class ResponseUsers(
    error: Boolean,
    message: String,
    val users: MutableList<User>,
    val answers: MutableList<Answer>
): Response(error, message)