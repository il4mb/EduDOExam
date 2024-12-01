package com.capstone.edudoexam.ui.dashboard.home

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.response.ResponseExams
import com.capstone.edudoexam.models.Exam

class HomeViewModel : ViewModel() {

    private val _exams: MutableLiveData<List<Exam>> = MutableLiveData()
    val exams: LiveData<List<Exam>> = _exams

    fun withExam(activity: FragmentActivity): Client<ExamsEndpoints, ResponseExams> {

        return Client<ExamsEndpoints, ResponseExams>(activity, ExamsEndpoints::class.java)
            .onSuccess {
                _exams.postValue(it.exams)
            }
    }

}