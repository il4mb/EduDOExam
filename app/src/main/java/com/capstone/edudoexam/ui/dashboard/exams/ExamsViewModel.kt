package com.capstone.edudoexam.ui.dashboard.exams

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.models.Exam

class ExamsViewModel : ViewModel() {
    private val _exams: MutableLiveData<List<Exam>> = MutableLiveData()
    val exams = _exams

    fun store(data: List<Exam>) {

        _exams.postValue(data)
    }
}