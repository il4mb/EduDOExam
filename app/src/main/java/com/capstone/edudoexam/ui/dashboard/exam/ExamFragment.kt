package com.capstone.edudoexam.ui.dashboard.exam

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.capstone.edudoexam.databinding.FragmentExamBinding

class ExamFragment : Fragment() {

    private lateinit var binding: FragmentExamBinding
    private var viewModel: ExamViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding   = FragmentExamBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[ExamViewModel::class]
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}