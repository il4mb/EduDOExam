package com.il4mb.edudoexam.api.payloads

import com.il4mb.edudoexam.models.Answer

data class AnswersPayload(
    val answers: MutableList<Answer>
)