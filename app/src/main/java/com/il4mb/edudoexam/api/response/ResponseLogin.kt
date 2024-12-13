package com.il4mb.edudoexam.api.response

class ResponseLogin(
    error: Boolean,
    message: String,
    val token: String?
): Response(error, message)