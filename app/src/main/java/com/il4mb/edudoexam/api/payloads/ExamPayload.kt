package com.il4mb.edudoexam.api.payloads

import java.util.Date

data class ExamPayload(
    val title: String,
    val subTitle: String,
    val startAt: Date?,
    val finishAt: Date?
)
