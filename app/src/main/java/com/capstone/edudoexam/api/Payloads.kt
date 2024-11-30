package com.capstone.edudoexam.api

data class RegisterPayload(
    val name: String,
    val gender: Int,
    val email: String,
    val password: String
)

data class LoginPayload(
    val email: String,
    val password: String
)

data class AddStoryPayload(
    val description: String,
    val photo: String,
    val lat: Float?,
    val lon: Float?
)