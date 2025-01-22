package com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.steps

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.databinding.FragmentFormQuestionStep4Binding
import com.il4mb.edudoexam.databinding.ViewFormQuestionOptionBinding
import com.il4mb.edudoexam.databinding.ViewQuestionOptionBinding
import com.il4mb.edudoexam.models.Option
import com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.FormViewModel
import io.noties.markwon.Markwon

class Step4: BaseFragment<FragmentFormQuestionStep4Binding>(FragmentFormQuestionStep4Binding::class.java),
    GenericListAdapter.ItemBindListener<Option, ViewQuestionOptionBinding> {

    private val markwon: Markwon by lazy {
        Markwon.create(requireContext())
    }
    private val liveModel: FormViewModel by activityViewModels()
    private val listAdapter: GenericListAdapter<Option, ViewQuestionOptionBinding> by lazy {
        GenericListAdapter(
            viewBindingClass = ViewQuestionOptionBinding::class.java,
            onItemBindCallback = this,
            diffCallback = GenericListAdapter.GenericDiffCallback { it.label }
        ).apply {
            setHasStableIds(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            recyclerOption.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            // Setup listeners
            durationPickerButton.setOnClickListener {
                liveModel.updateDuration(durationLabel.text.toString())
            }
        }

        // Observe LiveData
        setupObservers()
    }

    private fun setupObservers() {
        liveModel.apply {
            description.observe(viewLifecycleOwner) { newDescription ->
                markwon.setMarkdown(binding.questionDescription, newDescription)
            }
            duration.observe(viewLifecycleOwner) { newDuration ->
                binding.durationLabel.text = newDuration
            }
            image.observe(viewLifecycleOwner) { imageUri ->
                binding.questionImage.setImageURI(imageUri)
            }
            options.observe(viewLifecycleOwner) { optionsList ->
                listAdapter.submitList(optionsList.toList())
            }
        }
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewBind(binding: ViewQuestionOptionBinding, item: Option, position: Int) {
        binding.apply {
            optionLabel.text = "${item.label}."
            optionCheckbox.apply {
                isChecked = item.isCorrect
                setOnTouchListener { _, _ -> true } // prevent checkbox from being clicked
            }
            optionText.text = item.text

            root.background = if (item.isCorrect) {
                GradientDrawable().apply {
                    color =
                        ColorStateList.valueOf(requireContext().getColor(R.color.primary_variant))
                    cornerRadius = 23f
                }
            } else null

        }
    }
}