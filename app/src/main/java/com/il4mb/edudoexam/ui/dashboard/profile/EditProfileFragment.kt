package com.il4mb.edudoexam.ui.dashboard.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.ui.MenuLayout
import com.il4mb.edudoexam.databinding.FragmentProfileEditBinding

class EditProfileFragment : BaseFragment<FragmentProfileEditBinding>(FragmentProfileEditBinding::class.java) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(binding.userPhotoView, "user-photo")
    }

    override fun onCreateMenuItems(): MutableList<MenuLayout.MenuItem> {
        return mutableListOf(
            MenuLayout.MenuItem(requireContext()).apply {
                setImageDrawable(R.drawable.ui_tick)
                isEnabled = false
            }
        )
    }

}