package com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.steps

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.databinding.FragmentFormQuestionStep3Binding
import com.il4mb.edudoexam.tools.FragmentMediaHelper
import com.il4mb.edudoexam.tools.FragmentImageCropper
import com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.FormViewModel

class Step3: BaseFragment<FragmentFormQuestionStep3Binding>(FragmentFormQuestionStep3Binding::class.java) {

    private val imageCropper = FragmentImageCropper(this, null, null)
    private val fragmentMediaHelper = FragmentMediaHelper(this) { _, uri ->
        imageCropper.crop(uri) {
            liveData.updateImage(it)
        }
    }
    private val liveData: FormViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liveData.image.observe(viewLifecycleOwner) {
            binding.questionImage.setImageURI(it)
        }
        binding.apply {
            questionImage.setOnClickListener {
                fragmentMediaHelper.pickImageBooth()
            }
        }
    }
}