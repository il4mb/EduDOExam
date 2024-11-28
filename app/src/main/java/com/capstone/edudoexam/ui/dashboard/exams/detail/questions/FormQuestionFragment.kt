package com.capstone.edudoexam.ui.dashboard.exams.detail.questions

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.activity.addCallback
import androidx.core.view.get
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import com.capstone.edudoexam.components.AppFragment
import com.capstone.edudoexam.components.DialogBottom
import com.capstone.edudoexam.databinding.FragmentFormQuestionBinding

class FormQuestionFragment
    : AppFragment<FragmentFormQuestionBinding, FormViewModel>(FragmentFormQuestionBinding::inflate) {



    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            durationPickerButton.setOnClickListener {
                showDurationPicker()
            }

            imageCard.setOnClickListener{
                pickImageBooth()
            }
        }

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
            minValue = 0
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
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onImageResult(result: Boolean, uri: Uri) {
        if(result) {
            (binding.imageCard[0] as ImageView).setImageURI(uri)
        }
    }

    companion object {
        const val ARGS_QUESTION: String = "question"
    }

}