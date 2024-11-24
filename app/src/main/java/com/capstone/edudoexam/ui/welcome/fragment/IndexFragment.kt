package com.capstone.edudoexam.ui.welcome.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.capstone.edudoexam.R
import com.capstone.edudoexam.databinding.FragmentIndexBinding

class IndexFragment : Fragment() {

    lateinit var binding: FragmentIndexBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentIndexBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            loginButton.setOnClickListener {
                Navigation.findNavController(view).navigate(R.id.nav_login)
            }
            registerButton.setOnClickListener{
                Navigation.findNavController(view).navigate(R.id.nav_register)
            }
        }
    }
}