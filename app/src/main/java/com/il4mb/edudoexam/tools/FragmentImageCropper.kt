package com.il4mb.edudoexam.tools

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

class FragmentImageCropper(
    val fragment: Fragment,
    private val aspectX: Int?,
    private val aspectY: Int?
) {

    private var croppedImageUri: Uri? = null
    private var onCropImageCallback: ((Uri?) -> Unit)? = null
    private val cropImageLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onCropImageCallback?.invoke(croppedImageUri)
        } else {
            onCropImageCallback?.invoke(null)
        }
        croppedImageUri = null
        onCropImageCallback = null
    }
    private val context: Context by lazy {
        fragment.requireContext()
    }

    @SuppressLint("QueryPermissionsNeeded")
    internal fun crop(imageUri: Uri, callback: (Uri?) -> Unit) {
        try {
            val croppedImageFile = File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
            val outputUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", croppedImageFile)

            val cropIntent = Intent("com.android.camera.action.CROP").apply {
                setDataAndType(imageUri, "image/*")
                putExtra("crop", "true")
                putExtra("aspectX", aspectX)
                putExtra("aspectY", aspectY)
                putExtra("scale", true)
                putExtra("return-data", false)
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val resolveInfoList = context.packageManager.queryIntentActivities(cropIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            croppedImageUri = outputUri
            onCropImageCallback = callback
            cropImageLauncher.launch(cropIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }

}