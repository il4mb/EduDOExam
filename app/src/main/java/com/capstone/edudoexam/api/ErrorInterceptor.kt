package com.capstone.edudoexam.api

import androidx.fragment.app.FragmentActivity
import com.capstone.edudoexam.components.Utils.Companion.isInternetAvailable
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.ui.LoadingHandler
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.lang.ref.WeakReference

class ErrorInterceptor(activity: FragmentActivity) : Interceptor {

    private val activityReference = WeakReference(activity)

    override fun intercept(chain: Interceptor.Chain): Response {
        val activity = activityReference.get()

        if (activity != null && !isInternetAvailable(activity)) {
            activity.runOnUiThread {
                showNoInternetDialog(activity)
            }
            throw IOException("No internet connection") // Stop the request
        }

        return chain.proceed(chain.request())
    }

    private fun showNoInternetDialog(activity: FragmentActivity) {
        InfoDialog(activity)
            .setTitle("No Internet")
            .setMessage("Please check your connection and try again.")
            .show(InfoDialog.LENGTH_INDEFINITE)
        if(activity is LoadingHandler) {
            activity.setLoading(false)
        }
    }
}
