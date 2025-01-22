package com.il4mb.edudoexam.ui.dashboard.exams.detail.questions

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.models.Option

class FormViewModel : ViewModel() {
    private val _description = MutableLiveData<String>()
    private val _options = MutableLiveData<List<Option>>()
    private val _image = MutableLiveData<Uri?>()
    private val _duration = MutableLiveData<String>()

    init {
        _options.value = listOf(Option(Option.indexLetters(0), ""))
        _duration.value = "3.0"
    }

    val description: LiveData<String> get() = _description
    val options: LiveData<List<Option>> get() = _options
    val image: LiveData<Uri?> get() = _image
    val duration: LiveData<String> get() = _duration

    /**
     * Reindexes the options to ensure correct labels.
     */
    private fun reindexOptions(options: List<Option>): List<Option> {
        return options.mapIndexed { index, option ->
            option.copy(label = Option.indexLetters(index))
        }
    }

    /**
     * Updates the text of an option at the specified index.
     */
    fun updateOption(index: Int, option: Option) {
        val currentOptions = _options.value.orEmpty().toMutableList()
        if (index in currentOptions.indices) {
            currentOptions[index] = option
            _options.postValue(reindexOptions(currentOptions))
        }
    }

    /**
     * Adds a new option to the list.
     */
    fun addOption(option: Option) {
        val currentOptions = _options.value.orEmpty().toMutableList()
        currentOptions.add(option)
        _options.postValue(reindexOptions(currentOptions))
    }

    fun setCorrectOption(index: Int?) {
        val currentOptions = _options.value.orEmpty().toMutableList().mapIndexed { i, option ->
            option.copy(isCorrect = index == i)
        }
        _options.postValue(reindexOptions(currentOptions))
    }

    /**
     * Removes an option at the specified index.
     */
    fun removeOption(index: Int) {
        val currentOptions = _options.value.orEmpty().toMutableList()
        if (index in currentOptions.indices) {
            currentOptions.removeAt(index)
            _options.postValue(reindexOptions(currentOptions))
        }
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateDuration(duration: String) {
        _duration.value = duration
    }

    fun updateImage(uri: Uri?) {
        _image.value = uri
    }
}
