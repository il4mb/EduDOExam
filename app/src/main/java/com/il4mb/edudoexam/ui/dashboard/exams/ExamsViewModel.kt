package com.il4mb.edudoexam.ui.dashboard.exams

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ExamsEndpoints
import com.il4mb.edudoexam.api.response.ResponseExams
import com.il4mb.edudoexam.models.Exam

class ExamsViewModel : ViewModel() {
    private val _exams: MutableLiveData<List<Exam>> = MutableLiveData()
    val exams = _exams

    fun withExams(activity: FragmentActivity): Client<ExamsEndpoints, ResponseExams> {

        return Client<ExamsEndpoints, ResponseExams>(activity, ExamsEndpoints::class.java)
            .onSuccess {
                _exams.postValue(it.exams)
            }
    }
}