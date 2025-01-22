package com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.steps

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.ui.FloatingMenu
import com.il4mb.edudoexam.databinding.FragmentFormQuestionStep2Binding
import com.il4mb.edudoexam.databinding.ViewFormQuestionOptionBinding
import com.il4mb.edudoexam.models.Option
import com.il4mb.edudoexam.tools.Debouncer
import com.il4mb.edudoexam.ui.dashboard.exams.detail.questions.FormViewModel

class Step2 : BaseFragment<FragmentFormQuestionStep2Binding>(FragmentFormQuestionStep2Binding::class.java),
    GenericListAdapter.ItemBindListener<Option, ViewFormQuestionOptionBinding> {

    private val listAdapter: GenericListAdapter<Option, ViewFormQuestionOptionBinding> by lazy {
        GenericListAdapter(
            viewBindingClass = ViewFormQuestionOptionBinding::class.java,
            onItemBindCallback = this,
            diffCallback = OptionDiff()
        ).apply {
            setHasStableIds(true)
        }
    }
    private val liveModel: FormViewModel by activityViewModels()
    private var correctKey: Int? = null
        set(value) {
            field = value
            liveModel.setCorrectOption(value)
        }
    private val debounce = Debouncer()

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveModel.options.observe(viewLifecycleOwner) {
            debounce.debounce(20L) {
                listAdapter.submitList(it)
                binding.lengthView.text = "${it.size}/4"
                if(it.size == 4) {
                    binding.addOption.animate()
                        .alpha(0.1f)
                        .setDuration(50)
                        .withEndAction {
                            binding.addOption.isEnabled = false
                        }.start()
                } else {
                    binding.addOption.animate()
                        .alpha(0.4f)
                        .setDuration(50)
                        .withStartAction {
                            binding.addOption.isEnabled = true
                        }.start()
                }
            }
        }

        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            addOption.setOnClickListener {
                liveModel.addOption(Option(Option.indexLetters(listAdapter.itemCount), ""))
                debounce.debounce(80L) {
                    binding.recyclerView.recycledViewPool.clear()
                    listAdapter.notifyDataSetChanged()
                }
            }

            addOption.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> addOption.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(50).start()

                    MotionEvent.ACTION_UP -> addOption.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(50).start()
                }
                false
            }
        }
    }




    @SuppressLint("NotifyDataSetChanged")
    override fun onViewBind(binding: ViewFormQuestionOptionBinding, item: Option, position: Int) {

        binding.apply {
            // Highlight correct option
            root.background = if (correctKey == position) {
                GradientDrawable().apply {
                    color =
                        ColorStateList.valueOf(requireContext().getColor(R.color.primary_variant))
                    cornerRadius = 23f
                }
            } else null

            textInput.apply {
                text = item.text
                prefixText = "${Option.indexLetters(position)}. "
                onTextChanged { text ->
                    debounce.debounce(100L) {
                        liveModel.updateOption(position, item.apply {
                            this.text = text
                        })
                    }
                }
            }

            actionBtn.setOnClickListener { v ->
                FloatingMenu(requireActivity(), v).apply {
                    yOffset -= 100

                    if (correctKey != position) {
                        addItem("Mark as correct").apply {
                            icon = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.baseline_check_24
                            )
                            color = ContextCompat.getColor(requireContext(), R.color.primary)
                            setOnClickListener {
                                hide()
                                correctKey = position
                                this@Step2.binding.recyclerView.recycledViewPool.clear()
                                listAdapter.notifyDataSetChanged()

                            }
                        }
                    }
                    addItem("Delete").apply {
                        icon = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_delete_24
                        )
                        color = ContextCompat.getColor(requireContext(), R.color.danger)
                        setOnClickListener {
                            hide()
                            if (correctKey == position) correctKey = null
                            liveModel.removeOption(position)
                            debounce.debounce(80L) {
                                this@Step2.binding.recyclerView.recycledViewPool.clear()
                                listAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }.show()
            }
        }
    }

    class OptionDiff : DiffUtil.ItemCallback<Option>() {
        override fun areItemsTheSame(oldItem: Option, newItem: Option): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areContentsTheSame(oldItem: Option, newItem: Option): Boolean {
            return oldItem.label == newItem.label
        }
    }
}