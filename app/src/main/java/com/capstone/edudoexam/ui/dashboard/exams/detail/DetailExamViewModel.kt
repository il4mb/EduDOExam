package com.capstone.edudoexam.ui.dashboard.exams.detail

import android.util.Log
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
import com.google.gson.Gson

class DetailExamViewModel: ViewModel() {

    private val _exam = MutableLiveData<Exam>()
    val exam: LiveData<Exam> = _exam

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _blockedUsers = MutableLiveData<List<User>>()
    val blockedUsers: LiveData<List<User>> = _blockedUsers


    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    fun withExam(activity: FragmentActivity): Client<ExamsEndpoints, ResponseExam> {
        return Client<ExamsEndpoints, ResponseExam>(activity, ExamsEndpoints::class.java).onSuccess {
            _exam.postValue(it.exam)
        }
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

    fun setExam(exam: Exam) {
        Log.d("DetailExamViewModel", "setExam: ${Gson().toJson(exam)}")
        _exam.postValue(exam)
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

}