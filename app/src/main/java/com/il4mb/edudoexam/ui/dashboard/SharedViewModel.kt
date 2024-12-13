package com.il4mb.edudoexam.ui.dashboard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ProfileEndpoints
import com.il4mb.edudoexam.api.response.ResponseError
import com.il4mb.edudoexam.api.response.ResponseUser
import com.il4mb.edudoexam.models.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SharedViewModel : ViewModel() {

    private val _user: MutableLiveData<User?> = MutableLiveData()
    val user: LiveData<User?> = _user

    fun fetchUser(activity: FragmentActivity, onSuccessCallback: (User) -> Unit = {}, onErrorCallback: (ResponseError) -> Unit = {}) {
        Client<ProfileEndpoints, ResponseUser>(activity, ProfileEndpoints::class.java)
            .onError(onErrorCallback)
            .onSuccess {
                _user.postValue(it.user)
                it.user?.let { user -> onSuccessCallback(user) }
            }
            .fetch { it.getProfile() }
    }

    fun updateProfile(activity: FragmentActivity, newName: String, newGender: Int, newPhoto: File?, onErrorCallback: (ResponseError) -> Unit) {

        Client<ProfileEndpoints, ResponseUser>(activity, ProfileEndpoints::class.java)
            .onSuccess {
                _user.postValue(it.user)
            }
            .onError(onErrorCallback)
            .fetch { request ->
                val photo = newPhoto?.let { MultipartBody.Part.createFormData("photo", it.name, it.asRequestBody("image/*".toMediaTypeOrNull())) }
                request.updateProfile(
                    name   = newName.toRequestBody("text/plain".toMediaTypeOrNull()),
                    gender = newGender.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    photo  = photo
                )
            }
    }

    private val _topMargin = MutableLiveData<Int>()
    val topMargin: LiveData<Int> = _topMargin



    fun updateTopMargin(margin: Int) {
        _topMargin.postValue(margin)
    }
}
