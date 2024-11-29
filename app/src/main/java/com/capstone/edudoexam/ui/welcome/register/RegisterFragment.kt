package com.capstone.edudoexam.ui.welcome.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.capstone.edudoexam.R
import com.capstone.edudoexam.databinding.FragmentRegisterBinding
import com.capstone.edudoexam.ui.welcome.WelcomeActivity


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            navBackButton.setOnClickListener {
                Navigation.findNavController(view).popBackStack(R.id.nav_index, true)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        (requireActivity() as WelcomeActivity).setLoading(isLoading)
    }

}