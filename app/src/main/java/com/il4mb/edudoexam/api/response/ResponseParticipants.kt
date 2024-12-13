package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.Participant

class ResponseParticipants(
    error: Boolean,
    message: String,
    val participants: MutableList<Participant>
): Response(error, message)