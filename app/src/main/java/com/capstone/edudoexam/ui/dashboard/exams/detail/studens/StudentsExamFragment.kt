package com.capstone.edudoexam.ui.dashboard.exams.detail.studens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.components.FloatingMenu
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.UserDiffCallback
import com.capstone.edudoexam.databinding.FragmentStudentsExamBinding
import com.capstone.edudoexam.databinding.ViewItemUserBinding
import com.capstone.edudoexam.models.User

class StudentsExamFragment :
    Fragment(),
    GenericListAdapter.ItemBindListener<User, ViewItemUserBinding> {

    private val binding: FragmentStudentsExamBinding by lazy {
        FragmentStudentsExamBinding.inflate(layoutInflater)
    }
    private val viewModel: StudentsExamViewModel by viewModels()
    private val genericAdapter:  GenericListAdapter<User, ViewItemUserBinding> by lazy {
        GenericListAdapter(
            ViewItemUserBinding::class.java,
            onItemBindCallback = this,
            diffCallback = UserDiffCallback()
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericAdapter
            }
        }

        viewModel.apply {
            students.observe(viewLifecycleOwner) { students ->
                genericAdapter.submitList(students)
            }

            for (i in 1 until 100) {
                addStudent(User("$i", "John Doe $i", "john.c.calhoun@examplepetstore.com", 1))
            }

        }
        return binding.root
    }

    override fun onViewBind(binding: ViewItemUserBinding, item: User, position: Int) {
        binding.apply {
            userName.text = item.name
            userEmail.text = item.email

            root.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 18)
                    layoutParams = this
                }
                setOnClickListener {
                    FloatingMenu(requireContext(), it).apply {
                        xOffset = it.width / 2
                        yOffset = it.height / 2

                        addItem("Remove").apply {
                            icon = ContextCompat.getDrawable(context, R.drawable.baseline_person_remove_24)
                            color = Color.RED
                        }
                        addItem("Block").apply {
                            icon = ContextCompat.getDrawable(context, R.drawable.baseline_remove_circle_24)
                            setOnClickListener {  }
                        }
                    }
                        .show()
                }
            }
        }
    }
}