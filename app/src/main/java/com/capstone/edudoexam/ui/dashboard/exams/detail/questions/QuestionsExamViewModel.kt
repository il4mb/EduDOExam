package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.databinding.ViewItemQuestionBinding
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.models.User

class QuestionsExamViewModel : ViewModel() {
    private val _questions = MutableLiveData<MutableList<Question>>(mutableListOf())
    val questions: MutableLiveData<MutableList<Question>> = _questions

    fun moveItem(fromPosition: Int, toPosition: Int) {
        _questions.value?.let {
            val item = it.removeAt(fromPosition)
            it.add(toPosition, item)
            _questions.value = it
        }
    }

    fun addQuestion(question: Question) {
        _questions.value?.apply {
            add(question)
            _questions.value = this
        }
    }
}