package com.capstone.edudoexam.api.response

class ResponseLogin(
    error: Boolean,
    message: String,
    val token: String?
): Response(error, message)