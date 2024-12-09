package com.capstone.edudoexam.ui.exam.finish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.ExamFinishBinding
import com.capstone.edudoexam.ui.exam.ExamHelper
import com.capstone.edudoexam.ui.exam.ExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FinishFragment : Fragment() {

    private val liveModel: ExamViewModel by activityViewModels()
    private lateinit var binding: ExamFinishBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ExamFinishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        lifecycleScope.launch {
            delay(400)
            submitExamResult()
        }
    }

    private fun setupUI() {
        binding.apply {
            continueButton.setOnClickListener {
                requireActivity().finish()
            }
        }
    }

    private fun submitExamResult() {
        liveModel.submitAnswers(
            activity = requireActivity(),
            onSuccess = {
                showInfo(it.message)
            },
            onError = {
                showErrorDialog("Failed Submit Answer", it.message) { submitExamResult() }
            })
    }

    private fun showInfo(message: String) {
        InfoDialog(requireActivity())
            .setMessage(message)
            .show()
    }

    private fun showErrorDialog(title: String, message: String, retryAction: () -> Unit) {
        (requireActivity() as? ExamHelper)?.showErrorMessage(title, message, "Retry", retryAction)
    }
}