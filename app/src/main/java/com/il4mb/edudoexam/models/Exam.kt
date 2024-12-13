package com.il4mb.edudoexam.models

import java.util.Date

data class Exam(
    val id: String,
    val createdBy: String,
    val createdAt: Date,
    val startAt: Date,
    val finishAt: Date,
    val title: String,
    val subTitle: String,
    val users: MutableList<String>,
    val isOwner: Boolean,
    val isOngoing: Boolean,
    val isAnswered: Boolean,
    val answer: AnswerContainer?,
    val owner: User?
)
