package com.il4mb.edudoexam.models

data class Participant(
    val user: User,
    val answer: AnswerContainer?
)