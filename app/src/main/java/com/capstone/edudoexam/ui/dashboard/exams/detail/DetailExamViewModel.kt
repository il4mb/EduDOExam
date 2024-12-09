package com.capstone.edudoexam.ui.dashboard.exams.detail

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.api.response.ResponseExam
import com.capstone.edudoexam.api.response.ResponseQuestion
import com.capstone.edudoexam.api.response.ResponseQuestions
import com.capstone.edudoexam.api.response.ResponseUsers
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.models.User

class DetailExamViewModel: ViewModel() {

    private val _exam = MutableLiveData<Exam>()
    val exam: LiveData<Exam> = _exam
    fun setExam(exam: Exam) {
        _exam.postValue(exam)
    }

    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> = _users

    private val _blockedUsers = MutableLiveData<MutableList<User>>()
    val blockedUsers: LiveData<MutableList<User>> = _blockedUsers

    private val _questions = MutableLiveData<MutableList<Question>>()
    val questions: LiveData<MutableList<Question>> = _questions

    fun clearAll() {
        _users.postValue(mutableListOf())
        _questions.postValue(mutableListOf())
        _blockedUsers.postValue(mutableListOf())
    }

    fun withUsers(activity: FragmentActivity): Client<ExamsEndpoints, ResponseUsers> {
        return Client<ExamsEndpoints, ResponseUsers>(activity, ExamsEndpoints::class.java).onSuccess {
            _users.postValue(it.users)
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

    fun fetchBlockedUsers(activity: FragmentActivity, examId: String) {
       return withUsers(activity)
            .onError {  }
            .onSuccess {
                _blockedUsers.postValue(it.users)
            }
           .fetch { it.getStudents(examId, true) }
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