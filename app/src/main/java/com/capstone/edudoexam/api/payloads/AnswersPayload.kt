package com.capstone.edudoexam.api.payloads

import com.capstone.edudoexam.models.Answer

data class AnswersPayload(
    val answers: MutableList<Answer>
)