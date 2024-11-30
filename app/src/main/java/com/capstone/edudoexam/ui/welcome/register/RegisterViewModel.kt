package com.capstone.edudoexam.ui.welcome.register

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.capstone.edudoexam.api.ApiViewModel
import com.capstone.edudoexam.api.AuthEndpoints
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.RegisterPayload
import com.capstone.edudoexam.api.Response

class RegisterViewModel: ApiViewModel<Response>() {

    private val _response: MutableLiveData<Response> = MutableLiveData()
    val response: MutableLiveData<Response> = _response

    fun doRegister(fragment: FragmentActivity, name: String, gender: Int, email: String, password: String) {
        Client.beginWith(fragment, AuthEndpoints::class.java)
            .register(RegisterPayload(name, gender, email, password))
            .enqueue(this)
    }

    override fun onSuccess(response: Response) {
        _response.postValue(response)
    }

    override fun onError(error: Response) {
        _response.postValue(error)
    }

}
