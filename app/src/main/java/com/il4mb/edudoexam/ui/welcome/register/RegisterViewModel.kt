package com.il4mb.edudoexam.ui.welcome.register

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.il4mb.edudoexam.api.ApiViewModel
import com.il4mb.edudoexam.api.AuthEndpoints
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.api.payloads.Register

class RegisterViewModel: ApiViewModel<Response>() {

    private val _response: MutableLiveData<Response> = MutableLiveData()
    val response: MutableLiveData<Response> = _response

    fun doRegister(fragment: FragmentActivity, name: String, gender: Int, email: String, password: String) {
        Client.beginWith(fragment, AuthEndpoints::class.java)
            .register(Register(name, gender, email, password))
            .enqueue(this)
    }

    override fun onSuccess(response: Response) {
        _response.postValue(response)
    }

    override fun onError(error: Response) {
        _response.postValue(error)
    }

}
