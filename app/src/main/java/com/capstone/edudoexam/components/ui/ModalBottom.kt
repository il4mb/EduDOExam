package com.capstone.edudoexam.components.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.capstone.edudoexam.databinding.ViewModalBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottom : BottomSheetDialogFragment() {

    private var title: String?   = null
    private var message: String? = null
    private var layoutRes: Int? = null
    private lateinit var binding: ViewModalBottomBinding

    private var acceptAction: Action? = null
    private var declineAction: Action? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title   = it.getString(ARG_TITLE)
            message = it.getString(ARG_MESSAGE)
            layoutRes = it.getInt(ARG_LAYOUT, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ViewModalBottomBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(title != null && message != null) {
            binding.title.text = title
            binding.message.text = message
        } else if(layoutRes != 0) {
            binding.apply {
                layoutContainer.apply {
                    removeAllViews()
                    visibility = View.VISIBLE
                    addView(layoutInflater.inflate(layoutRes!!, null))
                }
            }
        }
        binding.apply {
            bottomsheetAccept.apply {
                acceptAction?.let { act ->
                    this.text = act.text
                    setOnClickListener {
                        if(act.callback.invoke()) {
                            this@ModalBottom.dismiss()
                        }
                    }
                    visibility = View.VISIBLE
                }
            }
            bottomsheetDecline.apply {
                declineAction?.let { act ->
                    this.text = act.text
                    setOnClickListener {
                        if(act.callback.invoke()) {
                            this@ModalBottom.dismiss()
                        }
                    }
                    visibility = View.VISIBLE
                }
            }
        }
    }

    fun setAcceptHandler(text: String, callback: () -> Boolean) {
        acceptAction = Action(text, callback)
    }

    fun setDeclineCallback(text: String, callback: () -> Boolean) {
        declineAction = Action(text, callback)
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_LAYOUT = "message"
        const val TAG = "ModalBottomSheet"
        fun create(title: String, message: String) = ModalBottom().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
            }
        }
        fun create(@LayoutRes layout: Int) = ModalBottom().apply {
            arguments = Bundle().apply {
                putInt(ARG_LAYOUT, layout)
            }
        }
    }

    data class Action(
        val text: String,
        val callback: () -> Boolean
    )


}