package com.capstone.edudoexam.ui.dashboard.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.capstone.edudoexam.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private var viewModel: HistoryViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding   = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[HistoryViewModel::class]
        return binding.root
    }
}