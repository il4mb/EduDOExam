package com.il4mb.edudoexam.ui.dashboard.exams.detail

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ExamsEndpoints
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.api.response.ResponseError
import com.il4mb.edudoexam.api.response.ResponseExam
import com.il4mb.edudoexam.api.response.ResponseParticipants
import com.il4mb.edudoexam.api.response.ResponseQuestion
import com.il4mb.edudoexam.api.response.ResponseQuestions
import com.il4mb.edudoexam.api.response.ResponseUsers
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.models.Participant
import com.il4mb.edudoexam.models.Question

class DetailExamViewModel: ViewModel() {

    private val _exam = MutableLiveData<Exam>()
    val exam: LiveData<Exam> = _exam

    private val _participants = MutableLiveData<MutableList<Participant>>()
    val participants: LiveData<MutableList<Participant>> = _participants

    private val _blockedParticipants = MutableLiveData<MutableList<Participant>>()
    val blockedUsers: LiveData<MutableList<Participant>> = _blockedParticipants

    private val _questions = MutableLiveData<MutableList<Question>>()
    val questions: LiveData<MutableList<Question>> = _questions


    fun fetchExam(
        activity: FragmentActivity,
        examId: String,
        success: () -> Unit = {},
        error: (ResponseError) -> Unit = {}) {

        return Client<ExamsEndpoints, ResponseExam>(activity, ExamsEndpoints::class.java)
            .onSuccess {
                _exam.postValue(it.exam)
                fetchQuestions(activity, examId, {
                    fetchParticipants(activity, examId, { success() }, error)
                }, error)
            }
            .onError(error)
            .fetch { it.getExam(examId) }

    }

    fun fetchParticipants(
        activity: FragmentActivity,
        examId: String,
        success: (MutableList<Participant>) -> Unit = {},
        error: (ResponseError) -> Unit = {}) {

        return Client<ExamsEndpoints, ResponseParticipants>(activity, ExamsEndpoints::class.java)
            .onSuccess {
                _participants.postValue(it.participants)
                success(it.participants)
            }
            .onError(error)
            .fetch { it.getParticipants(examId) }
    }


    fun fetchQuestions(
        activity: FragmentActivity,
        examId: String,
        success: (MutableList<Question>) -> Unit = {},
        error: (ResponseError) -> Unit = {}) {

        return Client<ExamsEndpoints, ResponseQuestions>(activity, ExamsEndpoints::class.java)
            .onError(error)
            .onSuccess {
                _questions.postValue(it.questions)
                success(it.questions)
            }
            .fetch { it.getQuestions(examId) }
    }



    fun clearAll() {
        _participants.postValue(mutableListOf())
        _questions.postValue(mutableListOf())
        _blockedParticipants.postValue(mutableListOf())
    }

    fun withUsers(activity: FragmentActivity): Client<ExamsEndpoints, ResponseUsers> {
        return Client<ExamsEndpoints, ResponseUsers>(activity, ExamsEndpoints::class.java).onSuccess {
           // _participants.postValue(it.users)
        }
    }

    fun withNoResult(activity: FragmentActivity): Client<ExamsEndpoints, Response> {
        return Client(activity, ExamsEndpoints::class.java)
    }

    fun withQuestions(activity: FragmentActivity): Client<ExamsEndpoints, ResponseQuestions> {
        return Client<ExamsEndpoints, ResponseQuestions>(activity, ExamsEndpoints::class.java).onSuccess {
             _questions.postValue(it.questions)
        }
    }

    fun withQuestion(activity: FragmentActivity): Client<ExamsEndpoints, ResponseQuestion> {
        return Client<ExamsEndpoints, ResponseQuestion>(activity, ExamsEndpoints::class.java)
            .onSuccess { res ->
            val question = res.question
            _questions.value = _questions.value?.toMutableList()?.apply {
                val index = indexOfFirst { it.id == question.id }
                if (index != -1) {
                    this[index] = question
                } else {
                    add(question)
                }
            }
        }
    }

    fun fetchBlockedParticipants(activity: FragmentActivity, examId: String, success: () -> Unit = {}, error: (ResponseError) -> Unit = {}) {
        Client<ExamsEndpoints, ResponseParticipants>(activity, ExamsEndpoints::class.java)
            .onError(error)
            .onSuccess {
                _blockedParticipants.postValue(it.participants)
                success()
            }
           .fetch { it.getParticipants(examId, true) }
    }

    fun addQuestion(question: Question, index: Int = -1) {
        _questions.value = _questions.value?.toMutableList()?.apply {
            if (index != -1 && index in indices) {
                this[index] = question
            } else {
                add(question)
            }
        }
    }

    fun moveQuestionItem(fromPosition: Int, toPosition: Int) {
        _questions.value?.let { questions ->
            // Swap the items
            val fromItem = questions[fromPosition]
            val toItem = questions[toPosition]

            questions[fromPosition] = toItem
            questions[toPosition] = fromItem

            // Update their orders
            questions[fromPosition].order = fromPosition + 1
            questions[toPosition].order = toPosition + 1

            // Notify the observers of the updated list
            _questions.value = ArrayList(questions)
        }
    }

    fun moveQuestion(fromPosition: Int, toPosition: Int, callback: (Question) -> Unit) {
        _questions.value?.let { questions ->
            val item = questions.removeAt(fromPosition)
            callback(item)
            questions.add(toPosition, item)
            questions.forEachIndexed { index, question ->
                question.order = index + 1
            }
            _questions.value = ArrayList(questions)
        }
    }

}