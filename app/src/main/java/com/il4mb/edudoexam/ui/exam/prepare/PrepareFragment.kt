package com.il4mb.edudoexam.ui.exam.prepare

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.NetworkStatusHelper
import com.il4mb.edudoexam.databinding.ExamPrepareBinding
import com.il4mb.edudoexam.ui.exam.ExamHelper
import com.il4mb.edudoexam.ui.exam.ExamViewModel
import com.il4mb.edudoexam.ui.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PrepareFragment : Fragment() {

    private lateinit var binding: ExamPrepareBinding
    private val liveModel: ExamViewModel by activityViewModels()

    private var isFetching = false
    private var isDataFetched = false
        set(value) {
            field = value
            updateDataIcons(if(!field) { IconState.FAILED } else IconState.SUCCESS)
            validateRequirements()
        }

    private var isInternetAvailable = false
        set(value) {
            field = value
            updateNetworkIcons(field)
            validateRequirements()
        }

    private var isCameraPermissionGranted = false
        set(value) {
            field = value
            updateCameraGrantIcons(field)
            validateRequirements()
        }

    private val networkStatusHelper: NetworkStatusHelper by lazy {
        NetworkStatusHelper(requireContext()) { isConnected ->
            isInternetAvailable = isConnected
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        handleCameraPermissionResult(isGranted)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ExamPrepareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveModel.exam.observe(viewLifecycleOwner) {
            isDataFetched = true
        }

        observeExamData()
        initializeState()

        lifecycleScope.launch {
            delay(400)
            if (isAdded) {
                if (isInternetAvailable()) {
                    showAnimatedContainer(binding.internetContainer) { showDataContainer() }
                } else {
                    showErrorDialog(
                        title = "No Internet Connection",
                        message = "An active internet connection is required to continue.",
                        actionText = "Retry",
                        retryAction = { checkInternetConnection() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        networkStatusHelper.startListening()
    }

    override fun onStop() {
        super.onStop()
        networkStatusHelper.stopListening()
    }

    private fun initializeState() {
        isInternetAvailable = false
        isDataFetched = false
        isCameraPermissionGranted = false
        binding.apply {
            internetContainer.visibility = View.GONE
            cameraContainer.visibility = View.GONE
            dataContainer.visibility = View.GONE
        }
    }

    private fun observeExamData() {
        liveModel.apply {
            exam.observe(viewLifecycleOwner) {
                binding.apply {
                    examTitle.text = it.title
                    examSubtitle.text = it.subTitle
                    examCode.text = it.id
                }
                fetchQuestions()
            }
            questions.observe(viewLifecycleOwner) {
                if(it.isNotEmpty()) {
                    isDataFetched = true
                } else {
                    showErrorDialog("Fetch Failed", "Questions not found", "Retry") { fetchData() }
                }
            }
        }
    }

    private fun fetchQuestions() {
        liveModel.fetchQuestion(requireActivity()) { error ->
            isDataFetched = false
            if(error.code == 404) {
                showErrorDialog("Fetch Failed", error.message, "Exit") {
                    requireActivity().finish()
                }
            } else {
                isFetching = false
                showErrorDialog("Fetch Failed", error.message, "Retry") {
                    requireActivity().finish()
                }
            }

        }
    }

    private fun fetchData() {
        if (isFetching) return
        isFetching = true
        updateDataIcons(IconState.LOADING)

        (requireActivity() as? ExamHelper)?.let { helper ->
            liveModel.fetchData(requireActivity(), helper.getExamId()) { error ->
                when(error.code) {
                    401 -> {
                        startActivity(Intent(requireContext(), WelcomeActivity::class.java))
                        requireActivity().finish()
                    }
                    404 -> {
                        requireActivity().finish()
                    }
                    else -> {
                        isFetching = false
                        lifecycleScope.launch {
                            delay(400)
                            showErrorDialog(
                                title = "Something went wrong",
                                message = error.message,
                                actionText = "Retry",
                                retryAction = { fetchData() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkInternetConnection() {
        if (isInternetAvailable()) {
            showAnimatedContainer(binding.internetContainer) { showDataContainer() }
        } else {
            showErrorDialog(
                title = "No Internet Connection",
                message = "An active internet connection is required to continue.",
                actionText = "Retry",
                retryAction = { checkInternetConnection() }
            )
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                isCameraPermissionGranted = true
                showAnimatedContainer(binding.cameraContainer)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showErrorDialog(
                    title = "Camera Permission Required",
                    message = "Camera access is required for the exam. Please grant permission to continue.",
                    actionText = "Retry",
                    retryAction = { requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun handleCameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            isCameraPermissionGranted = true
            showAnimatedContainer(binding.cameraContainer)
        } else {
            lifecycleScope.launch {
                delay(300)
                showErrorDialog(
                    title = "Permission Denied",
                    message = "Camera access is required to continue.",
                    actionText = "Retry",
                    retryAction = { checkCameraPermission() }
                )
            }
        }
    }

    private fun validateRequirements() {
        requireActivity().runOnUiThread {
            if (isInternetAvailable && isCameraPermissionGranted && isDataFetched) {
                binding.startExamButton.apply {
                    isEnabled = true
                    alpha = 1f
                    setOnClickListener {
                        findNavController().navigate(R.id.action_nav_prepare_to_nav_question, null,
                            NavOptions.Builder().setPopUpTo(findNavController().graph.startDestinationId, true).build())
                    }
                }
            } else {
                binding.startExamButton.apply {
                    isEnabled = false
                    alpha = 0.5f
                }

                if (!isInternetAvailable) checkInternetConnection()
                if (!isCameraPermissionGranted) checkCameraPermission()
                if (!isDataFetched) fetchData()
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun updateNetworkIcons(isConnected: Boolean) {
        val state = if (isConnected) IconState.SUCCESS else IconState.FAILED
        updateIcons(state, null, binding.internetIconFailed, binding.internetIconSuccess)
    }

    private fun updateCameraGrantIcons(isGranted: Boolean) {
        val state = if (isGranted) IconState.SUCCESS else IconState.FAILED
        updateIcons(state, null, binding.cameraIconFailed, binding.cameraIconSuccess)
    }

    private fun updateDataIcons(state: IconState) {
        binding.apply {
            dataIconLoading.visibility = if (state == IconState.LOADING) View.VISIBLE else View.GONE
            dataIconFailed.visibility = if (state == IconState.FAILED) View.VISIBLE else View.GONE
            dataIconSuccess.visibility = if (state == IconState.SUCCESS) View.VISIBLE else View.GONE
        }
    }

    private fun updateIcons(state: IconState, loading: View?, failed: View, success: View) {
        requireActivity().runOnUiThread {
            loading?.visibility = if (state == IconState.LOADING) View.VISIBLE else View.GONE
            failed.visibility = if (state == IconState.FAILED) View.VISIBLE else View.GONE
            success.visibility = if (state == IconState.SUCCESS) View.VISIBLE else View.GONE
        }
    }

    private fun showDataContainer() {
        binding.dataContainer.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY += 10f
            animate()
                .alpha(1f)
                .translationY(translationY - 10f)
                .setDuration(300)
                .withEndAction {
                    checkCameraPermission()
                    fetchData()
                }
                .start()
        }
    }

    private fun showAnimatedContainer(container: View, endAction: (() -> Unit)? = null) {
        container.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = 10f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .withEndAction { endAction?.invoke() }
                .start()
        }
    }

    private fun showErrorDialog(title: String, message: String, actionText: String, retryAction: () -> Unit) {
        (requireActivity() as? ExamHelper)?.showErrorMessage(title, message, actionText, retryAction)
    }

    enum class IconState { LOADING, FAILED, SUCCESS }

}
