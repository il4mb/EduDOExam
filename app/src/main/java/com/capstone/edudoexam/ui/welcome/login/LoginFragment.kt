package com.capstone.edudoexam.ui.welcome.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.capstone.edudoexam.api.AuthInterceptor
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.databinding.FragmentLoginBinding
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        viewModel.response.observe(viewLifecycleOwner) { response ->
            lifecycleScope.launch {
                delay(400)
                setLoading(false)
                if (response.error) {
                    Snackbar
                        .with(binding.root)
                        .show("Login Failed", response.message, Snackbar.LENGTH_LONG)
                } else {
                    Snackbar
                        .with(binding.root)
                        .show("Login Success", response.message, Snackbar.LENGTH_LONG)
                    AuthInterceptor.saveToken(requireActivity(), response.token!!)
                    startActivity(Intent(requireContext(), DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            loginButton.setOnClickListener {
                val email = inputEmail.editText?.text.toString()
                val password = inputPassword.editText?.text.toString()
                viewModel.doLogin(requireActivity(), email, password)
                setLoading(true)
            }
            skipLoginButton.setOnClickListener {
                val intent = Intent(context, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                activity?.finish()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        (requireActivity() as WelcomeActivity).setLoading(isLoading)
    }
}