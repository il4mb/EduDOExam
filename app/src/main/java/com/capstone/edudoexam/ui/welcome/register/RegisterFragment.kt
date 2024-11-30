package com.capstone.edudoexam.ui.welcome.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.databinding.FragmentRegisterBinding
import com.capstone.edudoexam.ui.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RegisterFragment : Fragment() {

    private val binding: FragmentRegisterBinding by lazy {
        FragmentRegisterBinding.inflate(layoutInflater)
    }
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.response.observe(viewLifecycleOwner) { response ->
            lifecycleScope.launch {
                delay(400)
                setLoading(false)
                if (response.error) {
                    Snackbar
                        .with(binding.root)
                        .show("Registration Failed", response.message, Snackbar.LENGTH_LONG)
                } else {
                    Snackbar
                        .with(binding.root)
                        .show("Registration Success", response.message, Snackbar.LENGTH_LONG)
                    delay(600)
                    findNavController().popBackStack(R.id.action_nav_register_to_nav_login, true)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            navBackButton.setOnClickListener {
                findNavController().popBackStack()
            }
            siginActionButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_nav_register_to_nav_login,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_register, true)
                        .build()
                )
            }

            inputName.onTextChanged {
                if(inputName.text.length < 3) {
                    inputName.error = "Name must be at least 3 characters"
                } else {
                    inputName.error = ""
                }
                formValidate()
            }
            inputEmail.onTextChanged {
                formValidate()
            }
            inputPassword.onTextChanged {
                formValidate()
            }
            inputConfirmPassword.onTextChanged {
                if(!isConfirmPasswordValid()) {
                    inputConfirmPassword.error = "Password does not match"
                } else {
                    inputConfirmPassword.error = ""
                }
                formValidate()
            }
            registerButton.setOnClickListener {
                setLoading(true)
                viewModel.doRegister(
                    requireActivity(),
                    inputName.text,
                    genderRadio.gender,
                    inputEmail.text,
                    inputPassword.text
                )
            }
        }
        formValidate()
    }

    private fun setLoading(isLoading: Boolean) {
        (requireActivity() as WelcomeActivity).setLoading(isLoading)
    }

    private fun formValidate() {
        binding.apply {
            registerButton.isEnabled =
                inputName.text.length > 3
                && inputEmail.text.isNotEmpty()
                && inputPassword.text.isNotEmpty()
                && inputConfirmPassword.text.isNotEmpty()
                && isConfirmPasswordValid()
                && isEmailValid()
        }
    }

    private fun isConfirmPasswordValid(): Boolean {
        return binding.inputPassword.text == binding.inputConfirmPassword.text
    }
    private fun isEmailValid(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text).matches()
    }
}