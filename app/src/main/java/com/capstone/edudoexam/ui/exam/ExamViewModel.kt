package com.capstone.edudoexam.ui.exam

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.payloads.AnswersPayload
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.api.response.ResponseError
import com.capstone.edudoexam.api.response.ResponseExam
import com.capstone.edudoexam.api.response.ResponseQuestions
import com.capstone.edudoexam.database.AppDatabase
import com.capstone.edudoexam.models.Answer
import com.capstone.edudoexam.models.Answer.Companion.toJson
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.models.Question
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class ExamViewModel: ViewModel() {

    private var database: WeakReference<AppDatabase>? = null
    fun setDatabase(database: AppDatabase) {
        this.database = WeakReference(database)
    }

    private val _answers: MutableLiveData<MutableMap<String, Answer>> = MutableLiveData()
    val answers: LiveData<MutableMap<String, Answer>> = _answers

    private val _currentQuestion: MutableLiveData<Question> = MutableLiveData()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _exam: MutableLiveData<Exam> = MutableLiveData()
    val exam: LiveData<Exam> = _exam

    private val _questions: MutableLiveData<MutableList<Question>> = MutableLiveData()
    val questions: LiveData<MutableList<Question>> = _questions

    fun fetchData(activity: FragmentActivity, examId: String, onError: (ResponseError) -> Unit) {
        Client<ExamsEndpoints, ResponseExam>(activity, ExamsEndpoints::class.java)
            .onError(onError)
            .onSuccess { _exam.postValue(it.exam) }
            .fetch { it.getExam(examId) }
    }

    fun fetchQuestion(activity: FragmentActivity, onError: (ResponseError) -> Unit) {
        _exam.value?.let { exam ->
            Client<ExamsEndpoints, ResponseQuestions>(activity, ExamsEndpoints::class.java)
                .onError(onError)
                .onSuccess {
                    _questions.postValue(it.questions)
                }
                .fetch { it.getQuestions(exam.id) }
        } ?: onError(ResponseError(true, "Exam not found", 404))
    }

    fun startExam() {
        if(_questions.value.isNullOrEmpty()) return
        if(_currentQuestion.value == null) {
            _currentQuestion.postValue(_questions.value?.first())
        }
    }

    fun goNext() {

        storeAnswerToDatabase()

        val questions = _questions.value ?: mutableListOf()
        val currentIndex = questions.indexOf(_currentQuestion.value)
        if (currentIndex < questions.size - 1) {
            _currentQuestion.postValue(questions[currentIndex + 1])
        } else {
            _currentQuestion.postValue(questions[0])
        }
    }

    fun submitAnswers(activity: FragmentActivity, onError: (ResponseError) -> Unit, onSuccess: (Response) -> Unit) {
        try {
            _exam.value?.let { exam ->
                (_answers.value ?: mutableMapOf()).let { answers ->

                    val answersList = answers.values.toMutableList()

                    Client<ExamsEndpoints, Response>(activity, ExamsEndpoints::class.java)
                        .onError(onError)
                        .onSuccess {

                            viewModelScope.launch(Dispatchers.IO) {
                                database?.get()?.let { db ->
                                    val answerDao = db.answerDao()
                                    answerDao.deleteExamId(exam.id)
                                }
                            }
                            onSuccess(it)
                        }
                        .fetch {
                            it.addAnswer(exam.id, AnswersPayload(answersList))
                        }
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun setChoice(choice: Char, examId: String) {
        val answers = (_answers.value ?: mutableMapOf())
        _currentQuestion.value?.let { currentQuestion ->
            if (!answers.containsKey(currentQuestion.id)) {
                answers[currentQuestion.id] =
                    Answer(currentQuestion.id, choice, HashMap<String, Int>().toJson, examId)
                _answers.postValue(answers)
            } else {
                answers[currentQuestion.id]?.choice = choice
                _answers.postValue(answers)
            }
        }

        Log.d("Answer", Gson().toJson(answers))
    }

    fun addSummary(label: String, examId: String) {
        val answers = (_answers.value ?: mutableMapOf())
        _currentQuestion.value?.let { currentQuestion ->
            if (!answers.containsKey(currentQuestion.id)) {
                answers[currentQuestion.id] =
                    Answer(currentQuestion.id, null, mutableMapOf(label to 1).toJson, examId)
                _answers.postValue(answers)
            } else {
                answers[currentQuestion.id]!!.apply {
                    summaryMap.apply {
                        if (containsKey(label)) {
                            this[label] = this[label]!! + 1
                        } else {
                            this[label] = 1
                        }
                        summaryMap = this
                    }
                }
                _answers.postValue(answers)
            }
        }

        Log.d("Answer", Gson().toJson(answers))
    }

    private fun storeAnswerToDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = database?.get()
            val answers = (_answers.value ?: mutableMapOf())
            val answersList = answers.values.toMutableList()
            if (answersList.isNotEmpty()) {
                db?.answerDao()?.insertAll(*answersList.toTypedArray())
            }
        }
    }
}