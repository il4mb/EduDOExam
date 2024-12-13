package com.il4mb.edudoexam.models

import java.util.Date

data class AnswerContainer(
    val examId: String,
    val userId: String,
    val createdAt: Date,
    val data: MutableList<Answer>
) {
    fun summaryAccumulation(): MutableMap<String, Int> {
        val summaries = data.map { it.summaryMap }
        val accumulated: MutableMap<String, Int> = mutableMapOf()
        for (summary in summaries) {
            for ((key, value) in summary) {
                accumulated[key] = accumulated.getOrDefault(key, 0) + value
            }
        }
        return accumulated
    }

    fun getTotalWrong(questions: MutableList<Question>): Int {
        return questions.size - getTotalCorrect(questions)
    }
    fun getTotalCorrect(questions: MutableList<Question>): Int {
        var score = 0
        questions.forEach { question ->
            data.find { it.questionId == question.id }?.let {
                if(it.choice == question.correctOption) {
                    score += 1
                }
            }
        }
        return score
    }
}

data class StudentExamResult(
    val id: String,
    val title: String,
    val subTitle: String,
    val createdAt: Date,
    val finishAt: Date,
    val startAt: Date,
    val createdBy: String,
    val isOwner: Boolean,
    val isOngoing: Boolean,
    val answer: AnswerContainer?,
    val user: User,
    val questions: MutableList<Question>
) {
    fun getTotalWrong(): Int {
        return questions.size - getTotalCorrect()
    }
    fun getTotalCorrect(): Int {
        var score = 0
        questions.forEach { question ->
            answer?.data?.find { it.questionId == question.id }?.let {
                if(it.choice == question.correctOption) {
                    score += 1
                }
            }
        }
        return score
    }
}

data class TeacherExamResult(
    val id: String,
    val title: String,
    val subTitle: String,
    val createdAt: Date,
    val finishAt: Date,
    val startAt: Date,
    val createdBy: String,
    val isOwner: Boolean,
    val isOngoing: Boolean,
    val answers: MutableList<AnswerContainer>,
    val users: MutableList<User>,
    val questions: MutableList<Question>
)
