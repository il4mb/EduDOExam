package com.capstone.edudoexam.api.response

import com.capstone.edudoexam.models.User

class ResponseUsers(
    error: Boolean,
    message: String,
    val users: MutableList<User>
): Response(error, message)