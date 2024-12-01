package com.capstone.edudoexam.ui.dashboard.exams.detail

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.response.ResponseQuestion
import com.capstone.edudoexam.api.response.ResponseUsers
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.models.User

class DetailExamViewModel: ViewModel() {

    private val _exam = MutableLiveData<Exam>()
    val exam: LiveData<Exam> = _exam

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    fun withUsers(activity: FragmentActivity): Client<ExamsEndpoints, ResponseUsers> {
        return Client<ExamsEndpoints, ResponseUsers>(activity, ExamsEndpoints::class.java).onSuccess {
            _users.postValue(it.users)
        }
    }

    fun withQuestions(activity: FragmentActivity): Client<ExamsEndpoints, ResponseQuestion> {
        return Client<ExamsEndpoints, ResponseQuestion>(activity, ExamsEndpoints::class.java).onSuccess {
             _questions.postValue(it.questions)
        }
    }

    fun setExam(exam: Exam) {
        _exam.postValue(exam)
    }
}