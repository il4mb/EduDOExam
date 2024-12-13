package com.il4mb.edudoexam.ui.exam.finish

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.databinding.ExamFinishBinding
import com.il4mb.edudoexam.ui.dashboard.DashboardActivity
import com.il4mb.edudoexam.ui.exam.ExamHelper
import com.il4mb.edudoexam.ui.exam.ExamViewModel
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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            DialogBottom.Builder(requireActivity()).apply {
                title = "Are you sure?"
                message = "You haven't finished the exam yet."
                dismissText = "Cancel"
                acceptText = "Confirm"
                dismissHandler = { true }
                acceptHandler = {
                    requireActivity().finish()
                    true
                }
                color = Color.parseColor("#FF0000")
            }.show()
        }
    }

    private fun setupUI() {
        binding.apply {
            continueButton.isEnabled = false
            continueButton.setOnClickListener {
                requireActivity().apply {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(DashboardActivity.ARGS_NAVIGATE_RESULT, liveModel.exam.value?.id ?: "")
                    })
                    finish()
                }
            }
        }
    }

    private fun submitExamResult() {
        liveModel.submitAnswers(
            activity = requireActivity(),
            onSuccess = {
                showInfo(it.message)
                DialogBottom.dismissAll(requireActivity())
                binding.continueButton.isEnabled = true
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