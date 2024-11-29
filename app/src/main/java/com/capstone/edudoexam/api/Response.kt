package com.capstone.edudoexam.api

open class Response(
    val error: Boolean,
    val message: String
)

class ResponseLogin(
    error: Boolean,
    message: String,
    val token: String?
): Response(error, message)

