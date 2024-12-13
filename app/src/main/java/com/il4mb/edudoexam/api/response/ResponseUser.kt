package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.User

class ResponseUser(
    error: Boolean,
    message: String,
    val user: User?
): Response(error, message)