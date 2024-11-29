package com.capstone.edudoexam.ui.dashboard.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.AppFragment
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.databinding.FragmentHomeBinding
import com.capstone.edudoexam.databinding.ViewItemExamBinding
import com.capstone.edudoexam.models.Exam
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment :
    AppFragment<FragmentHomeBinding>(FragmentHomeBinding::class.java),
    GenericListAdapter.ItemBindListener<Exam, ViewItemExamBinding> {

    private var data: ArrayList<Exam> = ArrayList()
    private lateinit var listAdapter: GenericListAdapter<Exam, ViewItemExamBinding>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = GenericListAdapter(
            ViewItemExamBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ExamDiffCallback()
        )
        for(i in 1..30) {
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
        getViewModel(HomeViewModel::class.java).exams.observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
        }
        binding.apply {
            recyclerView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        lifecycleScope.launch {
            delay(500)
            getParentActivity().apply {
                addMenu(R.drawable.man) {
                    findNavController().navigate(R.id.action_nav_home_to_nav_profile)
                }
                this.getBinding().appBarLayout.addContentView(JoinExamFormLayout(requireContext()).apply {

                    setOnClickListener {
                        findNavController().navigate(R.id.action_nav_home_to_nav_exam_detail)
                    }
                })
            }

            listAdapter.submitList(data)
        }
    }

    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {

        binding.apply {
            codeTextView.text = item.id
            titleView.text    = item.title
            subtitleView.text = item.subTitle
            dateTime.text     = item.startDate
            codeCopyButton.setOnClickListener {
                getViewModel(HomeViewModel::class.java).store(data)
                showToast("Code Copied")
            }
            root.apply {

                (layoutParams as MarginLayoutParams).let {
                    it.bottomMargin = 35
                    layoutParams = it
                }

                setOnClickListener {
                    findNavController().navigate(R.id.action_nav_home_to_nav_exam_detail)
                }
            }
        }
    }




    @SuppressLint("UseCompatLoadingForDrawables")
    class JoinExamFormLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : LinearLayout(context, attrs, defStyleAttr) {

        private val textInputLayout: TextInputLayout by lazy {
            TextInputLayout(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                hint = "Join Exam"
                endIconMode = TextInputLayout.END_ICON_CUSTOM
                endIconDrawable = context.getDrawable(R.drawable.outline_prompt_suggestion_24)
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE

                setBoxCornerRadii(14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat(), 14.dp.toFloat())
                addView(EditText(this.context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    setPadding(12.dp, 0, 12.dp, 0)
                    //background   = ContextCompat.getDrawable(context, R.drawable.rounded_frame)
                })
            }
        }

        init {
            // Set layout orientation and padding
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dp_14),
                resources.getDimensionPixelSize(R.dimen.dp_14),
                resources.getDimensionPixelSize(R.dimen.dp_14),
                resources.getDimensionPixelSize(R.dimen.dp_18)
            )

            // Add the TextInputLayout to the LinearLayout
            addView(textInputLayout)
        }
    }
}