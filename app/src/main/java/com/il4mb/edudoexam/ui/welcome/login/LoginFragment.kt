package com.il4mb.edudoexam.ui.welcome.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.api.AuthInterceptor
import com.il4mb.edudoexam.api.payloads.Login
import com.il4mb.edudoexam.components.Snackbar
import com.il4mb.edudoexam.databinding.FragmentLoginBinding
import com.il4mb.edudoexam.ui.dashboard.DashboardActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        viewModel.response.observe(viewLifecycleOwner) { response ->
            lifecycleScope.launch {
                if (!response.error){
                    Snackbar.with(binding.root).show("Login Success", response.message, Snackbar.LENGTH_LONG)
                    lifecycleScope.launch {
                        delay(400)
                        AuthInterceptor.saveToken(requireActivity(), response.token!!)
                        startActivity(Intent(requireContext(), DashboardActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            loginButton.setOnClickListener { doLogin() }
            registerButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_nav_login_to_nav_register,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_login, true)
                        .build()
                )
            }
        }
    }

    private fun doLogin() {
        val email = binding.inputEmail.text
        val password = binding.inputPassword.text
        viewModel.withLogin(requireActivity())
            .onError {
                lifecycleScope.launch {
                    Snackbar
                        .with(binding.root)
                        .show(
                            "Something went wrong",
                            it.message,
                            Snackbar.LENGTH_LONG
                        )
                }
            }
            .fetch { it.login(Login(email, password)) }
    }
}