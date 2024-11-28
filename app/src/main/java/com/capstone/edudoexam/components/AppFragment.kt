package com.capstone.edudoexam.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.capstone.edudoexam.R
import com.capstone.edudoexam.databinding.ActivityDashboard2Binding
import com.capstone.edudoexam.databinding.ViewModalPickImageBinding
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.dashboard.SharedViewModel
import java.io.File
import java.lang.reflect.ParameterizedType

abstract class AppFragment<T : ViewBinding, VM : ViewModel>(
    private val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> T
) : Fragment() {

    private var imageUri: Uri? = null
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted
            pickImageFromCamera()
        } else {
            // Permission denied
            val dialog = ModalBottom.create(
                getString(R.string.title_permission_denied),
                getString(R.string.description_permission_capture_denied)
            )
            dialog.show(parentFragmentManager, ModalBottom.TAG)
            dialog.setAcceptHandler(getString(R.string.get_it)) { true }
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            onImageResult(true, it)
        }
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                onImageResult(true, it)
            }
        }
    }

    internal fun pickImageFromCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            val uniqueFileName = "IMAGE-${System.currentTimeMillis()}.jpg"
            val imageFile      = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), uniqueFileName)
            imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)
            takePictureLauncher.launch(imageUri)

        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    internal fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    internal fun pickImageBooth() {
        DialogBottom.Builder(requireActivity())
            .apply {
                isAcceptActionButtonVisible = false
                isCancelActionButtonVisible = false
                setLayout(ViewModalPickImageBinding::class.java) { binding, dialog ->
                    binding.fromGallery.setOnClickListener {
                        pickImageFromGallery()
                        dialog.dismiss()
                    }
                    binding.fromCamera.setOnClickListener {
                        pickImageFromCamera()
                        dialog.dismiss()
                    }
                }
            }.show()
    }

    internal open fun onImageResult(result: Boolean, uri: Uri) {
        Log.d("IMAGE PICK", uri.toString())
    }


    private var _binding: T? = null
    protected val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }
    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[getViewModelClass()]
    }
    protected var containerToBottomAppbar = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container, false)
        sharedViewModel.topMargin.observe(viewLifecycleOwner) { margin ->
            val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = if(containerToBottomAppbar) margin else 0
            binding.root.layoutParams = layoutParams
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getViewModelClass(): Class<VM> {
        return (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[1] as Class<VM>
    }

    internal fun showToast(message: String) {
        requireContext().let {
            android.widget.Toast.makeText(it, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    internal fun getParentBinding() : ActivityDashboard2Binding {

        if(requireActivity() is DashboardActivity) {
            return (requireActivity() as DashboardActivity).getBinding()
        }
        throw IllegalStateException("Parent activity is not DashboardActivity")
    }


    internal val Int.dp: Int get() = (this * requireContext().resources.displayMetrics.density).toInt()

}