package com.il4mb.edudoexam.ui.dashboard.histories.teacher

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ExamsEndpoints
import com.il4mb.edudoexam.api.response.ResponseError
import com.il4mb.edudoexam.api.response.ResponseTeacherExamResult
import com.il4mb.edudoexam.models.TeacherExamResult

class ExamTeacherResultViewModel : ViewModel() {
    private val _result: MutableLiveData<TeacherExamResult> = MutableLiveData()
    val result: LiveData<TeacherExamResult> = _result

    fun fetchData(activity: FragmentActivity, examId: String, errorCallback: (ResponseError) -> Unit) {
        Client<ExamsEndpoints, ResponseTeacherExamResult>(activity, ExamsEndpoints::class.java)
            .onError(errorCallback)
            .onSuccess { _result.postValue(it.result) }
            .fetch { it.getExamResultForTeacher(examId) }
    }
}