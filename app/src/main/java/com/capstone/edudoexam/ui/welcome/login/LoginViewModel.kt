package com.capstone.edudoexam.ui.welcome.login

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.capstone.edudoexam.api.ApiViewModel
import com.capstone.edudoexam.api.AuthEndpoints
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.LoginPayload
import com.capstone.edudoexam.api.ResponseLogin


class LoginViewModel : ApiViewModel<ResponseLogin>() {

    private val _response: MutableLiveData<ResponseLogin> = MutableLiveData()
    val response: MutableLiveData<ResponseLogin> = _response

    fun doLogin(fragment: FragmentActivity, email: String, password: String) {
        Client.beginWith(fragment, AuthEndpoints::class.java)
            .login(LoginPayload(email, password))
            .enqueue(this)
    }

    override fun onSuccess(response: ResponseLogin) {
        _response.postValue(response)
    }

    override fun onError(error: com.capstone.edudoexam.api.Response) {
        _response.postValue(ResponseLogin(error.error, error.message, null))
    }

}