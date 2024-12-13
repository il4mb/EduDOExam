package com.il4mb.edudoexam.ui.welcome.register

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
import com.il4mb.edudoexam.components.Snackbar
import com.il4mb.edudoexam.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch


class RegisterFragment : Fragment() {

    private val binding: FragmentRegisterBinding by lazy {
        FragmentRegisterBinding.inflate(layoutInflater)
    }
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.response.observe(viewLifecycleOwner) { response ->
            lifecycleScope.launch {
                if (response.error) {
                    Snackbar
                        .with(binding.root)
                        .show("Registration Failed", response.message, Snackbar.LENGTH_LONG)
                } else {
                    Snackbar
                        .with(binding.root)
                        .show("Registration Success", response.message, Snackbar.LENGTH_LONG)
                    findNavController().navigate(R.id.action_nav_register_to_nav_login)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
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