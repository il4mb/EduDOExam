package com.il4mb.edudoexam.ui.welcome.login

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.AuthEndpoints
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.response.ResponseLogin


class LoginViewModel : ViewModel() {

    private val _response: MutableLiveData<ResponseLogin> = MutableLiveData()
    val response: MutableLiveData<ResponseLogin> = _response

    fun withLogin(fragment: FragmentActivity): Client<AuthEndpoints, ResponseLogin> {
        return Client<AuthEndpoints, ResponseLogin>(fragment, AuthEndpoints::class.java).onSuccess {
                _response.postValue(it)
            }
    }
}