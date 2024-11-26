package com.capstone.edudoexam.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _topMargin = MutableLiveData<Int>()
    val topMargin: LiveData<Int> = _topMargin

    fun updateTopMargin(margin: Int) {
        _topMargin.postValue(margin)
    }
}
