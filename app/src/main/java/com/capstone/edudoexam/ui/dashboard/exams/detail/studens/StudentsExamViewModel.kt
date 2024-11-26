package com.capstone.edudoexam.ui.dashboard.exams.detail.studens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.models.User

class StudentsExamViewModel: ViewModel() {

    private val _students: MutableLiveData<ArrayList<User>> = MutableLiveData()
    val students: LiveData<ArrayList<User>> = _students

    init {
        _students.value = ArrayList()
    }

    fun fetchStudents() {

    }

    fun addStudent(student: User) {
        _students.value?.add(student)
    }
}