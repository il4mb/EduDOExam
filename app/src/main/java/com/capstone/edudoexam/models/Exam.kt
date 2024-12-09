package com.capstone.edudoexam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

@Parcelize
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
    val isAnswered: Boolean
) : Parcelable
