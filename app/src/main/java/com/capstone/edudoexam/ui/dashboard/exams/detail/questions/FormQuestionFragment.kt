package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Rect
import android.net.Uri
import android.os.Build
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.capstone.edudoexam.components.BaseFragment
import com.capstone.edudoexam.components.DialogBottom
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils.Companion.CountWords
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.databinding.FragmentFormQuestionBinding
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FormQuestionFragment : BaseFragment<FragmentFormQuestionBinding>(FragmentFormQuestionBinding::class.java),
    ViewTreeObserver.OnGlobalLayoutListener {

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

    private val question: Question by lazy {
        arguments?.let {
            if (it.containsKey(ARGS_QUESTION)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable(ARGS_QUESTION, Question::class.java) ?:
                    Question("", "", "", null, 3.5, 'A', 0, mapOf()) // Default value if null
                } else {
                    @Suppress("DEPRECATION")
                    it.getParcelable(ARGS_QUESTION) ?:
                    Question("", "", "",  null,3.5, 'A', 0, mapOf()) // Default value if null
                }
            } else {
                Question("", "", "",  null,3.5, 'A', 0, mapOf()) // Default value if key is not present
            }
        } ?: Question("", "", "", null,3.5, 'A', 0, mapOf()) // Default value if arguments is null
    }

    private val viewModel: DetailExamViewModel by viewModels()

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            root.viewTreeObserver.addOnGlobalLayoutListener(this@FormQuestionFragment)
            question.apply {
                durationLabel.text = "${duration.asDurationFormatted} Minute"
                questionDescription.editText?.setText(description)
                optionsLayout.options = options.asQuestionOptions
            }
            durationPickerButton.setOnClickListener {
                showDurationPicker()
            }
            imageCard.setOnClickListener{
                pickImageBooth()
            }

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
                correctOption = question.correctOption
            }

            questionDescription.editText?.doOnTextChanged { _, _, _, _ ->

                if(isDescriptionValid) {
                    questionDescription.error = null
                } else {
                    questionDescription.error = "Description must be at least 3 words"
                }
                validateForm()
            }

            saveButton.setOnClickListener {
                if(question.examId.isEmpty()) {
                    if(question.id.isEmpty()) {
                        saveChanges()
                    } else {
                        updateChanges()
                    }
                }
            }
        }

        lifecycleScope.launch {
            delay(400)
            setLoading(false)
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
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

        val inputDuration = getDuration() / 60

        return  isDurationValid && isDescriptionValid && isOptionValid && (
                inputDuration != question.duration
                || binding.questionDescription.editText?.text.toString().trim() != question.description.trim()
                || (!imageUri.isNullOrEmpty() && imageUri != question.image)
                || binding.optionsLayout.options.a.trim() != question.options['A']?.trim()
                || binding.optionsLayout.options.b.trim() != question.options['B']?.trim()
                || binding.optionsLayout.options.c.trim() != question.options['C']?.trim()
                || binding.optionsLayout.options.d.trim() != question.options['D']?.trim())
    }

    private fun saveChanges() {
        val duration = getDuration() / 60
        val description = binding.questionDescription.editText?.text.toString()
        val options = binding.optionsLayout.options


        setLoading(true)
        viewModel.withQuestions(requireActivity())
            .onError {
                lifecycleScope.launch {
                    delay(400)
                    Snackbar.with(binding.root)
                        .show("Something went wrong", it.message, Snackbar.LENGTH_LONG)
                    setLoading(false)
                }
            }

    }

    private fun updateChanges() {
        setLoading(true)
        viewModel.withQuestions(requireActivity())
            .onError {
                lifecycleScope.launch {
                    delay(400)
                    Snackbar.with(binding.root)
                        .show("Something went wrong", it.message, Snackbar.LENGTH_LONG)
                    setLoading(false)
                }
            }

    }

    companion object {
        const val ARGS_QUESTION: String = "question"
    }

}