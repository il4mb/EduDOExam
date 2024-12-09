package com.capstone.edudoexam.api.payloads

import java.time.LocalDateTime
import java.util.Date

data class ExamPayload(
    val title: String,
    val subTitle: String,
    val startAt: Date?,
    val finishAt: Date?
)
