package com.capstone.edudoexam.ui.dashboard.histories

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.response.ResponseExams
import com.capstone.edudoexam.models.Exam

class HistoriesViewModel : ViewModel() {
    private val _histories: MutableLiveData<List<Exam>> = MutableLiveData()
    val histories = _histories

    fun withHistories(activity: FragmentActivity): Client<ExamsEndpoints, ResponseExams> {
        return Client<ExamsEndpoints, ResponseExams>(activity, ExamsEndpoints::class.java)
            .onSuccess {
                _histories.postValue(it.exams)
            }
            .onError {
                Log.d("API ERROR,", it.message)
            }

    }
}