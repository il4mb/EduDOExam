package com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.databinding.FragmentFormQuestionStep1Binding
import com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.FormViewModel

class Step1: BaseFragment<FragmentFormQuestionStep1Binding>(FragmentFormQuestionStep1Binding::class.java) {

    private val liveModel: FormViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            markwonEditor.apply {
                addOnChangedListener {
                    liveModel.updateDescription(it)
                }
            }
        }
    }
}