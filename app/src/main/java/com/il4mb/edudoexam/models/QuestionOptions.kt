package com.il4mb.edudoexam.models

import com.google.gson.Gson

data class QuestionOptions (
    val a: String,
    val b: String,
    val c: String,
    val d: String,
) {
    fun asMap(): Map<Char, String> {
        return mapOf(
            'A' to a,
            'B' to b,
            'C' to c,
            'D' to d
        )
    }

    fun asJson(): String {
        return Gson().toJson(asMap())
    }

}