package com.capstone.edudoexam.ui.exam.question

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.ExamQuestionBinding
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.exam.ExamHelper
import com.capstone.edudoexam.ui.exam.ExamViewModel
import com.capstone.edudoexam.ui.exam.FaceAnalyzer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

class QuestionFragment : Fragment() {

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted
            launchCamera()
        } else {
            showErrorDialog(
                title = "Permission Denied",
                message = "Camera access is required to continue.",
                retryAction = { startCamera() }
            )
        }
    }
    private val faceCallback: FaceAnalyzer.FaceDetectionCallback by lazy {
        object : FaceAnalyzer.FaceDetectionCallback {
            override fun onFace(bitmap: Bitmap, label: String, confidence: Double) {
                if(isVisible) {
                    (requireActivity() as? ExamHelper)?.let {
                        liveModel.addSummary(label, it.getExamId())
                    }
                    binding.faceNotDetectState.visibility = View.GONE
                }
            }
            override fun onNoFace() {
                binding.faceNotDetectState.visibility = View.VISIBLE
            }
        }
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), FaceAnalyzer(requireContext(), faceCallback))
                }
            try {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    private fun launchCamera()  {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun showErrorDialog(title: String, message: String, retryAction: () -> Unit) {

        DialogBottom.dismissAll(requireActivity())

        DialogBottom
            .Builder(requireActivity())
            .apply {
                this.title = title
                this.message = message
                acceptText = "Retry"
                acceptHandler = {
                    retryAction()
                    true
                }
            }
            .show().apply {
                dismissible = false
            }
    }

    private val liveModel: ExamViewModel by activityViewModels()
    private lateinit var binding: ExamQuestionBinding
    private var countdownTimer: CountDownTimer? = null
    private var secondsRemaining: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ExamQuestionBinding.inflate(inflater, container, false)
        binding.apply {
            listOf(optionA, optionB, optionC, optionD).forEach { node ->
                node.setOnClickListener {
                    (requireActivity() as? ExamHelper)?.let {
                        liveModel.setChoice(node.label, it.getExamId())
                    }
                }
            }
        }
        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchCamera()
        (requireActivity() as? QuestionHelper)?.onCountDown(0)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            DialogBottom.Builder(requireActivity()).apply {
                title = "Are you sure?"
                message = "You haven't finished the exam yet. If you leave, you won't be able to continue the exam."
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
        liveModel.startExam()
    }

    private fun updateSelectedOption(choice: Char?) {
        binding.apply {
            listOf(optionA, optionB, optionC, optionD).forEach { node ->
                if(choice == null) {
                    node.isActive = false
                } else {
                    node.isActive = choice == node.label
                }
            }
            listOf(optionA, optionB, optionC, optionD).find { it.isActive }?.let { showIfHideNextButton() } ?: run { questionNextButton.visibility = View.GONE }

        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupObservers() {
        liveModel.apply {
            currentQuestion.observe(viewLifecycleOwner) { question ->
                setupQuestionUI(question)
                startCountdown((question.duration * 60).toInt())
                lifecycleScope.launch {
                    delay(400)
                    binding.questionNextButton.text = if(isLastQuestion()) "Finish" else "Next"
                }
            }
            answers.observe(viewLifecycleOwner) { answers ->
                updateSelectedOption(currentQuestion.value.let { questions ->
                    answers[questions?.id].let {
                        it?.choice
                    }
                })
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun setupQuestionUI(question: Question) {
        binding.apply {
            if (question.image.isNullOrEmpty()) {
                questionImageWrapper.visibility = View.GONE
            } else {
                questionImageWrapper.visibility = View.VISIBLE
                if (questionImageWrapper.childCount > 0) {
                    Glide.with(requireContext())
                        .load(question.image)
                        .into((questionImageWrapper[0] as ImageView))
                }
            }
            questionDescription.text = question.description
            questionOrder.text = "${question.order}."
            optionA.text = question.options['A'] ?: ""
            optionB.text = question.options['B'] ?: ""
            optionC.text = question.options['C'] ?: ""
            optionD.text = question.options['D'] ?: ""
            faceNotDetectState.setOnTouchListener { _, _ ->  true }
            questionNextButton.setOnClickListener { goNext() }
        }
    }

    private fun showIfHideNextButton() {
        binding.questionNextButton.apply {
            val defaultText = text.toString()
            if (visibility != View.VISIBLE) {
                isEnabled = false
                visibility = View.VISIBLE
                alpha = 0f
                translationX -= 20
                animate()
                    .translationX(translationX + 20)
                    .alpha(1f)
                    .setDuration(500)
                    .start()

                var secondsRemaining = 10
                postDelayed(object : Runnable {
                    @SuppressLint("SetTextI18n")
                    override fun run() {
                        if (secondsRemaining > 0) {
                            text = "$secondsRemaining seconds"
                            secondsRemaining--
                            postDelayed(this, 1000)
                        } else {
                            text = defaultText
                            isEnabled = true
                        }
                    }
                }, 1000)
            }
        }
    }

    private fun startCountdown(durationInSeconds: Int) {
        countdownTimer?.cancel()
        secondsRemaining = durationInSeconds
        createCountdownTimer(secondsRemaining.toLong()).start()
    }

    private fun createCountdownTimer(seconds: Long): CountDownTimer {
        return object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = (millisUntilFinished / 1000).toInt()
                (requireActivity() as? QuestionHelper)?.onCountDown(secondsRemaining)
            }

            override fun onFinish() {
                (requireActivity() as? QuestionHelper)?.onCountDown(0)
                goNextOrFinish()
            }
        }.also { countdownTimer = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
        ProcessCameraProvider.getInstance(requireContext()).get().unbindAll() // Ensure resources are released.
    }

    private fun goNext() {
        val activated = binding.let { listOf(it.optionA, it.optionB, it.optionC, it.optionD).find { item -> item.isActive } }
        if(activated != null) {
            goNextOrFinish()
        } else {
            InfoDialog(requireActivity()).apply {
                setMessage("Please select an option")
            }.show()
        }
    }

    private fun goNextOrFinish() {
        if (isLastQuestion()) {
            finishExam()
        } else {
            liveModel.goNext()
        }
    }

    private fun isLastQuestion(): Boolean {
        val totalQuestion   = liveModel.questions.value?.last()?.order ?: 0
        val currentQuestion = liveModel.currentQuestion.value?.order ?: 0
        return totalQuestion > 0 && currentQuestion > 0 && totalQuestion == currentQuestion
    }

    private fun finishExam() {
        findNavController().navigate(
            R.id.action_nav_question_to_finishFragment, null,
            NavOptions.Builder().setPopUpTo(findNavController().graph.startDestinationId, true).build())
    }

    interface QuestionHelper {
        fun onCountDown(seconds: Int)
    }
}
