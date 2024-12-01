package com.capstone.edudoexam.api.response

import com.capstone.edudoexam.models.User

class ResponseUser(
    error: Boolean,
    message: String,
    val user: User?
): Response(error, message)