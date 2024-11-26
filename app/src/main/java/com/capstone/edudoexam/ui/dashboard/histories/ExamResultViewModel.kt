package com.capstone.edudoexam.ui.dashboard.histories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.models.ExamResult

class ExamResultViewModel : ViewModel() {
    private val _history: MutableLiveData<ExamResult> = MutableLiveData()
    val history: LiveData<ExamResult> = _history

    fun update(history: ExamResult) {
        _history.postValue(history)
    }
}