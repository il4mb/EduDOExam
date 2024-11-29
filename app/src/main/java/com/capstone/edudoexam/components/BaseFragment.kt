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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.capstone.edudoexam.R
import com.capstone.edudoexam.databinding.ViewModalPickImageBinding
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.dashboard.SharedViewModel
import java.io.File
import java.lang.reflect.Method

abstract class BaseFragment<T : ViewBinding>(private val viewBindingClass: Class<T>) : Fragment() {

    internal var isBottomNavigationVisible = false
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

    private var _binding: T? = null

    protected val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    private fun pickImageFromCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            val uniqueFileName = "IMAGE-${System.currentTimeMillis()}.jpg"
            val imageFile      = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), uniqueFileName)
            imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)
            takePictureLauncher.launch(imageUri)

        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun pickImageFromGallery() {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = getInflateBinding()(null, inflater, container, false) as T
        sharedViewModel.topMargin.observe(viewLifecycleOwner) { margin ->
            val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = margin
            binding.root.layoutParams = layoutParams
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                getParentActivity().getAppbar().apply {
                    addContentView(onAppbarContentView())
                }
                if(isBottomNavigationVisible) {
                    getParentActivity().showNavBottom()
                } else {
                    getParentActivity().hideNavBottom()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getInflateBinding(): Method {
        return viewBindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
    }

    internal fun showToast(message: String) {
        requireContext().let {
            android.widget.Toast.makeText(it, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    internal fun getParentActivity() : DashboardActivity {
        return requireActivity() as DashboardActivity
    }

    internal fun <T: ViewModel> getViewModel(viewModelClass: Class<T>): T {
        return ViewModelProvider(requireActivity())[viewModelClass]
    }

    internal open fun onAppbarContentView() : View? {
        return null
    }
}