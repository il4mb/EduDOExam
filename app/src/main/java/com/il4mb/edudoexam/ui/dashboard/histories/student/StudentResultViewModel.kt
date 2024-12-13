package com.il4mb.edudoexam.ui.dashboard.histories.student

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ExamsEndpoints
import com.il4mb.edudoexam.api.response.ResponseError
import com.il4mb.edudoexam.api.response.ResponseStudentAnswer
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.models.Participant
import com.il4mb.edudoexam.models.Question
import com.il4mb.edudoexam.models.User

class StudentResultViewModel : ViewModel() {

    private val _exam: MutableLiveData<Exam> = MutableLiveData()
    val exam: LiveData<Exam> = _exam

    private val _questions: MutableLiveData<MutableList<Question>> = MutableLiveData()
    val questions: LiveData<MutableList<Question>> = _questions

    private val _participant: MutableLiveData<Participant> = MutableLiveData()
    val participant: LiveData<Participant> = _participant

    fun prepare(exam: Exam, questions: MutableList<Question>, participant: Participant) {
        _exam.postValue(exam)
        _questions.postValue(questions)
        _participant.postValue(participant)
    }

    fun loadData(activity: FragmentActivity, examId: String, user: User, succeed: () -> Unit, failed: (ResponseError) -> Unit) {
        Client<ExamsEndpoints, ResponseStudentAnswer>(activity, ExamsEndpoints::class.java)
            .onError { failed(it) }
            .onSuccess {
                _participant.postValue(
                    Participant(
                        user, it.answer
                    )
                )
                _exam.postValue(it.exam)
                _questions.postValue(it.questions)
                succeed()
            }
            .fetch { it.getStudentAnswer(examId) }
    }


//    private val _result: MutableLiveData<com.capstone.edudoexam.models.StudentExamResult> = MutableLiveData()
//    val result: LiveData<com.capstone.edudoexam.models.StudentExamResult> = _result
//
//    fun fetchData(activity: FragmentActivity, examId: String) {

//    }

}