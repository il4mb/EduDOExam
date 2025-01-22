package com.il4mb.edudoexam.tools

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.components.ui.ModalBottom
import com.il4mb.edudoexam.databinding.ViewModalPickImageBinding
import java.io.File

class FragmentMediaHelper(
    private val fragment: Fragment,
    private val onImageResult: (Boolean, Uri) -> Unit
) {
    private var imageUri: Uri? = null
    private val cameraPermissionLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted
            pickImageFromCamera()
        } else {
            // Permission denied
            val dialog = ModalBottom.create(
                fragment.getString(R.string.title_permission_denied),
                fragment.getString(R.string.description_permission_capture_denied)
            )
            dialog.show(fragment.parentFragmentManager, ModalBottom.TAG)
            dialog.setAcceptHandler(fragment.getString(R.string.get_it)) { true }
        }
    }
    private val pickImageLauncher   = fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {

            val uniqueFileName = "IMAGE-${System.currentTimeMillis()}.jpg"
            val destinationFile = File(fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), uniqueFileName)

            try {
                fragment.requireContext().contentResolver.openInputStream(it)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                imageUri = FileProvider.getUriForFile(
                    fragment.requireContext(),
                    "${fragment.requireContext().packageName}.provider",
                    destinationFile
                )

                onImageResult(true, imageUri!!)
            } catch (e: Exception) {
                e.printStackTrace()
                onImageResult(false, it)
            }
        }
    }
    private val takePictureLauncher = fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                onImageResult(true, it)
            }
        }
    }

    internal fun pickImageFromCamera() {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            val uniqueFileName = "IMAGE-${System.currentTimeMillis()}.jpg"
            val imageFile      = File(fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), uniqueFileName)
            imageUri = FileProvider.getUriForFile(fragment.requireContext(), "${fragment.requireContext().packageName}.provider", imageFile)
            takePictureLauncher.launch(imageUri)

        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    internal fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    internal fun pickImageBooth() {
        DialogBottom.Builder(fragment.requireActivity())
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

}

