package com.il4mb.edudoexam.api.payloads

data class Register(
    val name: String,
    val gender: Int,
    val email: String,
    val password: String
)