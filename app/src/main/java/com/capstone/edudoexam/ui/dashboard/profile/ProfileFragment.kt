package com.capstone.edudoexam.ui.dashboard.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.capstone.edudoexam.components.AppFragment
import com.capstone.edudoexam.databinding.FragmentProfileBinding

class ProfileFragment : AppFragment<FragmentProfileBinding, ProfileViewModel>(FragmentProfileBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        containerToBottomAppbar = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}