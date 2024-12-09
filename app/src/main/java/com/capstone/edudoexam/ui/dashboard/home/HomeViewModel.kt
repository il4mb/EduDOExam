package com.capstone.edudoexam.ui.dashboard.home

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.response.ResponseError
import com.capstone.edudoexam.api.response.ResponseExams
import com.capstone.edudoexam.models.Exam

class HomeViewModel : ViewModel() {

    private val _upcomingExams: MutableLiveData<List<Exam>> = MutableLiveData()
    val upcomingExams: LiveData<List<Exam>> = _upcomingExams

    private val _ongoingExams: MutableLiveData<List<Exam>> = MutableLiveData()
    val ongoingExams: LiveData<List<Exam>> = _ongoingExams

    fun withExam(activity: FragmentActivity): Client<ExamsEndpoints, ResponseExams> {

        return Client<ExamsEndpoints, ResponseExams>(activity, ExamsEndpoints::class.java)
            .onSuccess {
                _upcomingExams.postValue(it.exams)
            }
    }
    fun fetchOnGoing(activity: FragmentActivity, onError: (ResponseError) -> Unit) {
        Client<ExamsEndpoints, ResponseExams>(activity, ExamsEndpoints::class.java)
            .onError(onError)
            .onSuccess {
                _ongoingExams.postValue(it.exams)
            }
            .fetch { it.getOngoingExams() }
    }
}