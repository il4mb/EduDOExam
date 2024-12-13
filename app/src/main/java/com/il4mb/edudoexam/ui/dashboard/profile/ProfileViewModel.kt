package com.il4mb.edudoexam.ui.dashboard.profile

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ProfileEndpoints
import com.il4mb.edudoexam.api.response.ResponseUser

class ProfileViewModel : ViewModel() {

    private val _profile = MutableLiveData<ResponseUser>()
    val response: LiveData<ResponseUser> = _profile

    fun withProfile(activity: FragmentActivity): Client<ProfileEndpoints, ResponseUser> {
        return Client<ProfileEndpoints, ResponseUser>(activity, ProfileEndpoints::class.java).onSuccess {
            _profile.postValue(it)
        }
    }

}