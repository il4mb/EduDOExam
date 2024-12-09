package com.capstone.edudoexam.ui.dashboard.histories.student

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints

class ExamStudentResultViewModel : ViewModel() {

    private val _result: MutableLiveData<com.capstone.edudoexam.models.StudentExamResult> = MutableLiveData()
    val result: LiveData<com.capstone.edudoexam.models.StudentExamResult> = _result

    fun fetchData(activity: FragmentActivity, examId: String) {
        Client<ExamsEndpoints, com.capstone.edudoexam.api.response.ResponseStudentExamResult>(activity, ExamsEndpoints::class.java)
            .onSuccess { _result.postValue(it.result) }
            .fetch { it.getExamResultForStudent(examId) }
    }

}