package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.activity.addCallback
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.capstone.edudoexam.api.payloads.QuestionPayload
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils.Companion.CountWords
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.FragmentFormQuestionBinding
import com.capstone.edudoexam.models.QuestionOptions
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FormQuestionFragment : BaseFragment<FragmentFormQuestionBinding>(FragmentFormQuestionBinding::class.java),
    ViewTreeObserver.OnGlobalLayoutListener {

    private val formState: FormStateData by lazy {
        FormStateData(binding)
    }

    private val isDescriptionValid: Boolean
        get() {
            val inputDescription = binding.questionDescription.editText?.text.toString()
            return inputDescription.CountWords >= 3
        }
    private val isOptionValid: Boolean
        get() {
            return binding.optionsLayout.options.run {
                a.length > 1 && b.length > 1 && c.length > 1 && d.length > 1
            }
        }

    private val isDurationValid: Boolean
        get() {
            val inputDuration = getDuration() / 60
            return inputDuration >= 3.0
        }

    private var imageUri: String? = null
        set(value) {
            field = value
            (binding.imageCard[0] as ImageView).setImageURI(Uri.parse(field))
        }

    private var questionId: String = ""

    private val examId: String by lazy {
        arguments?.getString(ARGS_QUESTION_EXAM_ID)?: ""
    }

    private val viewModel: DetailExamViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         arguments?.apply {
            questionId = getString(ARGS_QUESTION_ID) ?: ""
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            Log.d("QUESTION ID", questionId)
            try {
                questions.find { it.id == questionId }?.let {
                    binding.apply {
                        formState.duration = it.duration
                        formState.description = it.description
                        formState.imageUri = it.image
                        formState.options = it.options
                        formState.correctOption = it.correctOption
                        formState.order = it.order
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            lifecycleScope.launch {
                delay(600)
                setLoading(false)
            }
        }

        binding.apply {
            root.viewTreeObserver.addOnGlobalLayoutListener(this@FormQuestionFragment)
            durationPickerButton.setOnClickListener { showDurationPicker() }
            imageCard.setOnClickListener{
                pickImageBooth()
            }
            questionDescription.apply {
                editText?.doOnTextChanged { _, _, _, _ ->

                    if(isDescriptionValid) {
                        questionDescription.error = null
                    } else {
                        questionDescription.error = "Description must be at least 3 words"
                    }
                    validateForm()
                }
            }
            optionsLayout.apply {
                setOnChangedCallback {
                    validateForm()
                }
            }

            saveButton.setOnClickListener {
                if(questionId.isNotEmpty() && examId.isNotEmpty()) {
                    updateQuestion()
                } else {
                    saveNewQuestion()
                }
            }
        }

        lifecycleScope.launch {
            delay(200)
            setLoading(false)
            binding.apply {
                questionDescription.requestFocus()
                optionsLayout.apply {
                    setOnFocusChangeListener { v, hasFocus ->
                        Log.d("Focus", "hasFocus: $hasFocus")
                        if (hasFocus) {
                            nestedScrollView.post {
                                nestedScrollView.smoothScrollTo(0, nestedScrollView.bottom)
                            }
                        }
                    }
                    setOnChangedCallback { editText ->
                        if(editText?.text.toString().isNotEmpty()) {
                            editText?.error = null
                        } else {
                            editText?.error = "Option must be not empty"
                        }
                        nestedScrollView.post {
                            nestedScrollView.smoothScrollTo(0, nestedScrollView.bottom)
                        }
                        validateForm()
                    }
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

                if (formIsValid()) {
                    val dialog = DialogBottom.Builder(requireActivity())
                        .apply {
                            title = "Are you sure?"
                            message = "Do you really want to leave?, Changes will not be saved."
                            dismissText = "Cancel"
                            acceptText = "Confirm"
                            dismissHandler = { true }
                            acceptHandler = {
                                findNavController().popBackStack()
                                true
                            }
                        }
                    dialog.show()
                } else findNavController().popBackStack()
            }
        }
    }

    /**
     * Convert duration to double
     * @return duration in seconds
     */
    private fun getDuration(): Double {
        val duration = binding.durationLabel.text.toString().replace(Regex("[^0-9:]"), "")
        val parts = duration.split(":")
        return if (parts.size == 2) {
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0
            (minutes * 60 + seconds).toDouble()
        } else {
            0.0
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showDurationPicker() {

        val duration = getDuration()
        val initialMinutes = (duration / 60).toInt()
        val initialSeconds = (duration % 60).toInt()

        val minutesPicker = NumberPicker(requireContext()).apply {
            minValue = 3
            maxValue = 59
            value = initialMinutes
        }
        val secondsPicker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = 59
            value = initialSeconds
        }
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16.dp)
            addView(minutesPicker)
            addView(secondsPicker)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Duration (MM:SS)")
            .setView(dialogLayout)
            .setPositiveButton("OK") { _, _ ->
                val minutes = minutesPicker.value
                val seconds = secondsPicker.value
                binding.durationLabel.text = String.format("%d:%02d Minute", minutes, seconds)
                validateForm()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onImageResult(result: Boolean, uri: Uri) {
        if(result) {
            imageUri = uri.toString()
            validateForm()
        }
    }

    override fun onGlobalLayout() {
        try {
            binding.apply {
                val rect = Rect()
                root.getWindowVisibleDisplayFrame(rect)
                val screenHeight = binding.root.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    binding.saveButton.translationY = -keypadHeight.toFloat()
                } else {
                    binding.saveButton.translationY = 0f
                }
            }
        } catch (_: Throwable) {
            // silent is perfect
        }
    }

    private fun validateForm() {
        binding.saveButton.apply {
            val currentTranslationY = translationY
            if (formIsValid()) {
                if(!isVisible) {
                    visibility = View.VISIBLE
                    alpha = 0f
                    translationY = currentTranslationY + 10f
                    animate()
                        .alpha(1f)
                        .translationY(currentTranslationY)
                        .setDuration(300)
                        .start()
                }
            } else {
                if(isVisible) {
                    animate()
                        .alpha(0f)
                        .translationY(currentTranslationY + 10f)
                        .setDuration(300)
                        .withEndAction{
                            visibility = View.GONE
                        }
                        .start()
                }
            }
        }
    }

    private fun formIsValid(): Boolean {
        // Calculate input duration in minutes
        val inputDuration = getDuration() / 60

        // Validate if duration has changed
        val isDurationChanged = inputDuration != formState.duration

        // Validate if description has changed
        val isDescriptionChanged = binding.questionDescription.editText?.text.toString().trim() != formState.description?.trim()

        // Validate if the image URI has changed
        val isImageChanged = !imageUri.isNullOrEmpty() && imageUri != formState.imageUri

        // Validate if any of the options have changed
        val areOptionsChanged = binding.optionsLayout.options.run {
            a.trim() != formState.options?.get('A')?.trim() ||
                    b.trim() != formState.options?.get('B')?.trim() ||
                    c.trim() != formState.options?.get('C')?.trim() ||
                    d.trim() != formState.options?.get('D')?.trim()
        }

        // Validate if the correct option has changed
        val isCorrectOptionChanged = binding.optionsLayout.correctOption != formState.correctOption

        // Combine all validations into a single condition
        return isDurationValid && isDescriptionValid && isOptionValid &&
                (isDurationChanged || isDescriptionChanged || isImageChanged || areOptionsChanged || isCorrectOptionChanged)
    }

    private fun saveNewQuestion() {

        val order = formState.order ?: 0
        if(examId.isEmpty()) {
            InfoDialog(requireActivity()).setMessage("Missing exam id").show()
            return
        }

        val duration    = getDuration() / 60
        val description = binding.questionDescription.editText?.text.toString()
        val options     = binding.optionsLayout.options.asMap()
        val correctOption = binding.optionsLayout.correctOption

        val payload = QuestionPayload(description, imageUri, duration, correctOption, options, order)

        setLoading(true)
        viewModel.withQuestion(requireActivity())
            .onError { onErrorHandler(it) }
            .onSuccess {
                InfoDialog(requireActivity())
                    .setTitle("Success")
                    .setMessage("new question added to exam")
                    .show()
                questionId = it.question.id
            }
            .fetch { it.addQuestion(examId, payload) }

    }

    private fun updateQuestion() {

        val order = formState.order ?: 0
        if(examId.isEmpty() || questionId.isEmpty()) {
            InfoDialog(requireActivity()).setMessage("Missing Exam id or question id").show()
            return
        }

        val duration    = getDuration() / 60
        val description = binding.questionDescription.editText?.text.toString()
        val options     = binding.optionsLayout.options.asMap()
        val correctOption = binding.optionsLayout.correctOption

        val payload = QuestionPayload(description, imageUri, duration, correctOption, options, order)

        setLoading(true)
        viewModel.withQuestions(requireActivity())
            .onError { onErrorHandler(it) }
            .onSuccess {
                InfoDialog(requireActivity())
                    .setMessage("Question updated")
                    .show()
            }
            .fetch { it.updateQuestion(examId, questionId, payload) }

    }

    private fun onErrorHandler(e: Response) {
        lifecycleScope.launch {
            delay(400)
            setLoading(false)
            InfoDialog(requireActivity())
                .setTitle("Something went wrong")
                .setMessage(e.message)
                .show()
        }
    }

    companion object {
        const val ARGS_QUESTION_EXAM_ID: String = "question-exam-id"
        const val ARGS_QUESTION_ID: String = "question-id"

        private val Double.asDurationFormatted: String
            @SuppressLint("DefaultLocale")
            get() {
                val minutes = this.toInt() // Get the integer part of the number (minutes)
                val seconds = ((this - minutes) * 60).toInt() // Calculate the remaining seconds
                return String.format("%02d:%02d", minutes, seconds)
            }

       private val Map<Char, String>.asQuestionOptions: QuestionOptions
            get() = QuestionOptions(
                a = this['A'] ?: "",
                b = this['B'] ?: "",
                c = this['C'] ?: "",
                d = this['D'] ?: ""
            )
    }

    class FormStateData(val binding: FragmentFormQuestionBinding) {

        var duration : Double? = null
            @SuppressLint("SetTextI18n")
            set(value) {
                field = value
                binding.durationLabel.text = "${value?.asDurationFormatted} Minute"
            }
        var description : String? = null
            set(value) {
                field = value
                binding.questionDescription.editText?.setText(value)
            }
        var imageUri : String? = null
            set(value) {
                field = value
                if(field != null) (binding.imageCard[0] as ImageView).setImageURI(Uri.parse(field))
                else (binding.imageCard[0] as ImageView).setImageURI(null)
            }
        var options : Map<Char, String>? = null
            set(value) {
                field = value?.apply {
                    binding.optionsLayout.options = this.asQuestionOptions
                }
            }
        var correctOption : Char? = null
            set(value) {
                field = value
                binding.optionsLayout.correctOption = value?: 'A'
            }
        var order : Int? = null

    }
}