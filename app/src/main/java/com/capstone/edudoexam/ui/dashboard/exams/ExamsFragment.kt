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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.BaseFragment
import com.capstone.edudoexam.components.DialogBottom
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.ModalBottom
import com.capstone.edudoexam.components.Utils.Companion.getAttr
import com.capstone.edudoexam.databinding.FragmentExamsBinding
import com.capstone.edudoexam.databinding.ViewItemExamBinding
import com.capstone.edudoexam.databinding.ViewPopupLayoutBinding
import com.capstone.edudoexam.models.Exam
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
    private var data: ArrayList<Exam> = ArrayList()
    private lateinit var listAdapter: GenericListAdapter<Exam, ViewItemExamBinding>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isBottomNavigationVisible = true

        listAdapter = GenericListAdapter(
            ViewItemExamBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
        for(i in 1..15) {
            data.add(
                Exam(
                    "DUMMY-$i",
                    "November 25, 2024 at 1:32:29 PM UTC+7",
                    "November 23, 2024 at 5:05:56 PM UTC+7",
                    "XT63TAP4XA",
                    "Ujian Tengah Semester ${(i+1)/10}",
                    "Kelas ${(i+1)/6}")
            )
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getParentActivity().showNavBottom()

        getViewModel(ExamsViewModel::class.java).exams.observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
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
            getViewModel(ExamsViewModel::class.java).store(data)
        }
    }

    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {
        binding.apply {
            codeTextView.text = item.id
            titleView.text = item.title
            subtitleView.text = item.subTitle
            dateTime.text = item.startDate
            codeCopyButton.setOnClickListener {
                getViewModel(ExamsViewModel::class.java).store(data)
                showToast("Code Copied")
            }
            (root.layoutParams as MarginLayoutParams).let {
                it.bottomMargin = 35
                root.layoutParams = it
            }

            root.setOnClickListener {
                findNavController().navigate(R.id.action_nav_exams_to_nav_exam_detail)
            }
        }
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