package com.capstone.edudoexam.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.capstone.edudoexam.ui.dashboard.SharedViewModel
import java.lang.reflect.ParameterizedType

abstract class AppFragment<T : ViewBinding, VM : ViewModel>(
    private val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> T
) : Fragment() {

    private var _binding: T? = null
    protected val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }
    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[getViewModelClass()]
    }
    protected var containerToBottomAppbar = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container, false)
        sharedViewModel.topMargin.observe(viewLifecycleOwner) { margin ->
            val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = if(containerToBottomAppbar) margin else 0
            binding.root.layoutParams = layoutParams
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getViewModelClass(): Class<VM> {
        return (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[1] as Class<VM>
    }

    internal fun showToast(message: String) {
        requireContext().let {
            android.widget.Toast.makeText(it, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}