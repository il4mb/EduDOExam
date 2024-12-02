package com.capstone.edudoexam.models

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
}