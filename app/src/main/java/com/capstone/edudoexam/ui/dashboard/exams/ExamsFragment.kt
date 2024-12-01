package com.capstone.edudoexam.ui.dashboard.exams

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.BaseFragment
import com.capstone.edudoexam.components.DialogBottom
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils.Companion.getAttr
import com.capstone.edudoexam.databinding.FragmentExamsBinding
import com.capstone.edudoexam.databinding.ViewItemExamBinding
import com.capstone.edudoexam.databinding.ViewPopupLayoutBinding
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExamsFragment :
    BaseFragment<FragmentExamsBinding>(FragmentExamsBinding::class.java),
    GenericListAdapter.ItemBindListener<Exam, ViewItemExamBinding> {

    private val startRotateAnim: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.start_rotate)
    }

    private val endRotateAnim: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.end_rotate)
    }

    private val listAdapter: GenericListAdapter<Exam, ViewItemExamBinding> by lazy {
        GenericListAdapter(
            ViewItemExamBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
    }

    private val viewModel: ExamsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBottomNavigationVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.exams.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                delay(600)
                setLoading(false)
                listAdapter.submitList(it)
            }
        }
        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        lifecycleScope.launch {
            delay(300)
            getParentActivity().apply {
                addMenu(
                    R.drawable.baseline_add_24,
                    getAttr(requireContext(), android.R.attr.textColor)
                ) { toggleAddMenu(it) }
            }
            fetchExams()
        }
    }

    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {
        binding.apply {
            codeTextView.text = item.id
            titleView.text = item.title
            subtitleView.text = item.subTitle
            dateTime.text = item.simplifiedStartDate
            codeCopyButton.setOnClickListener {
                showToast("Code Copied")
            }
            (root.layoutParams as MarginLayoutParams).let {
                it.bottomMargin = 35
                root.layoutParams = it
            }

            root.setOnClickListener {
                if(item.isOwner) {
                    findNavController().navigate(R.id.action_nav_exams_to_nav_exam_detail, Bundle().apply {
                      putParcelable(DetailExamFragment.ARG_EXAM, item)
                    })
                }
            }
        }
    }

    private fun fetchExams() {
        setLoading(true)
        viewModel.withExams(requireActivity())
            .onError {
                lifecycleScope.launch {
                    delay(600)
                    Snackbar.with(binding.root).show("Something went wrong", it.message, Snackbar.LENGTH_LONG)
                    setLoading(false)
                }
            }
            .fetch { it.getExams() }
    }

    @SuppressLint("ServiceCast")
    private fun toggleAddMenu(v: View) {
        val layoutInflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ViewPopupLayoutBinding.inflate(layoutInflater)
        val popUp = PopupWindow(v).apply {
            contentView = binding.root
            width = LinearLayout.LayoutParams.WRAP_CONTENT
            height = LinearLayout.LayoutParams.WRAP_CONTENT
            isFocusable = true
            //animationStyle = R.style.popup_window_animation
            setBackgroundDrawable(ColorDrawable())
        }

        val location = IntArray(2)
        v.getLocationOnScreen(location)

        val xOffset = 400
        val yOffset = 100

        popUp.showAtLocation(binding.root, Gravity.NO_GRAVITY, location[0] + xOffset, location[1] + yOffset)
        popUp.setOnDismissListener {
            v.startAnimation(endRotateAnim)
        }
        binding.actionCreate.setOnClickListener {
            popUp.dismiss()
            createExam()
        }

        binding.actionJoin.setOnClickListener {
            popUp.dismiss()
            joinExam()
        }

        v.startAnimation(startRotateAnim)
    }

    private fun createExam() {
        DialogBottom.Builder(requireActivity()).apply {
            title = "Accept User Terms and Conditions"
            acceptText = "Accept"
        }.show()
    }

    private fun joinExam() {
        DialogBottom.Builder(requireActivity()).apply {
            title = "Please Enter Exam Code"
            acceptText = "Join"
        }.show()
    }
}