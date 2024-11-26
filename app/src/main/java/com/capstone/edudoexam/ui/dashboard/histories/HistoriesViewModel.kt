package com.capstone.edudoexam.ui.dashboard.histories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.models.ExamResult

class HistoriesViewModel : ViewModel() {
    private val _histories: MutableLiveData<List<ExamResult>> = MutableLiveData()
    val histories = _histories

    fun store(data: List<ExamResult>) {

        histories.postValue(data)
    }
}