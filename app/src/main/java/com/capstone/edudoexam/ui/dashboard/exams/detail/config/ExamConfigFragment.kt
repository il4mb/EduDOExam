package com.capstone.edudoexam.ui.dashboard.exams.detail.config

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.payloads.ExamPayload
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.ui.FloatingMenu
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.UserDiffCallback
import com.capstone.edudoexam.components.Utils.Companion.copyTextToClipboard
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getColor
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.FragmentExamConfigBinding
import com.capstone.edudoexam.databinding.ViewItemUserBinding
import com.capstone.edudoexam.models.User
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExamConfigFragment : BaseFragment<FragmentExamConfigBinding>(FragmentExamConfigBinding::class.java),
    GenericListAdapter.ItemBindListener<User, ViewItemUserBinding>,
    ViewTreeObserver.OnGlobalLayoutListener {

    private var examId: String? = null
    private var examTextTitle: String? = null
    private var examTextSubtitle: String? = null
    private val listAdapter: GenericListAdapter<User, ViewItemUserBinding> by lazy {
        GenericListAdapter(
            ViewItemUserBinding::class.java,
            onItemBindCallback = this,
            diffCallback = UserDiffCallback()
        )
    }
    private val viewModel: DetailExamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        examId = arguments?.getString(ARG_EXAM_ID)
        examTextTitle = arguments?.getString(ARG_EXAM_TITLE)
        examTextSubtitle = arguments?.getString(ARG_EXAM_SUBTITLE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.blockedUsers.observe(viewLifecycleOwner) {
            if(it.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.blockedUsersRecycle.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.blockedUsersRecycle.visibility = View.VISIBLE
            }
            listAdapter.submitList(it)
            lifecycleScope.launch {
                delay(800)
                setLoading(false)
            }
        }
        binding.apply {

            root.viewTreeObserver.addOnGlobalLayoutListener(this@ExamConfigFragment)

            examTitle.apply {
                text = examTextTitle.toString()
                onTextChanged { validateForm() }
            }
            examSubtitle.apply {
                text = examTextSubtitle.toString()
                onTextChanged { validateForm() }
            }
            examCode.apply {
                text = examId?.uppercase().toString()
                onClickAtEnd {
                    if(copyTextToClipboard(requireContext(), examId?.uppercase().toString())) {
                        Toast.makeText(context, "Exam code copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            blockedUsersRecycle.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            saveButton.setOnClickListener {
                doSaveChanges()
            }
        }

        examId?.let {
            lifecycleScope.launch {
                viewModel.fetchBlockedUsers(requireActivity(), it)
            }
        }
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
                    setMargins(0, 0, 0, 18.dp)
                    layoutParams = this
                }
                actionButton.apply {
                    visibility = View.VISIBLE
                    setOnClickListener { showItemMenu(item, actionButton) }
                }
            }
        }
    }

    private fun doUnblockUser(user: User) {
        examId?.let {
            setLoading(true)
            viewModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    InfoDialog(requireActivity())
                        .setMessage("${user.name} removed from blocked users")
                        .show()
                    viewModel.fetchBlockedUsers(requireActivity(), examId!!)
                    viewModel.withUsers(requireActivity()).fetch { it.getStudents(examId!!, false) }
                }
                .fetch { it.updateStudent(examId!!, user.id, false) }
        }
    }

    private fun doSaveChanges() {
        examId?.let {
            val newTitle = binding.examTitle.text
            val newSubtitle = binding.examSubtitle.text
            setLoading(true)
            viewModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    InfoDialog(requireActivity())
                        .setMessage("Exam updated")
                        .show()
                    setLoading(false)
                    examTextTitle = newTitle
                    examTextSubtitle = newSubtitle
                    validateForm()
                }
                .fetch { it.updateExam(examId!!, ExamPayload(newTitle, newSubtitle)) }

        }
    }

    private fun showUnblockDialog(item: User) {
        DialogBottom.Builder(requireActivity()).apply {
            color = getColor(requireContext(), R.color.danger)
            title = "Are you sure?"
            message = "Are you sure you want to unblock user from this exam?\nUser detail:\nName	: ${item.name}\nEmail	: ${item.email}\nThis action cannot be undone."
            acceptText = "Unblock"
            acceptHandler = {
                doUnblockUser(item)
                true
            }
        }.show()
    }

    private fun showItemMenu(item: User, anchor: View) {
        anchor.animate()
            .rotation(180f)
            .setDuration(150)
            .start()

        FloatingMenu(requireContext(), anchor).apply {

            val floatingMenu = this

            onDismissCallback = {
                anchor.animate()
                    .rotation(0f)
                    .setDuration(150)
                    .start()
            }

            xOffset = -300
            yOffset = 80

            addItem("Remove From Block").apply {
                color = getColor(context, R.color.danger)
                setOnClickListener {
                    showUnblockDialog(item)
                    floatingMenu.hide()
                }
            }
        }.show()
    }

    private fun onErrorHandler(e: Response) {
        lifecycleScope.launch {
            delay(400)
            setLoading(false)
            InfoDialog(requireActivity())
                .setTitle("Something went wrong")
                .setMessage(e.message)
                .show()
        }
    }

    private fun validateForm() {
        binding.saveButton.apply {
            val currentTranslationY = translationY
            if (formIsValid()) {
                if(!isVisible) {
                    visibility = View.VISIBLE
                    alpha = 0f
                    translationY = currentTranslationY + 10f
                    animate()
                        .alpha(1f)
                        .translationY(currentTranslationY)
                        .setDuration(300)
                        .start()
                }
            } else {
                if(isVisible) {
                    animate()
                        .alpha(0f)
                        .translationY(currentTranslationY + 10f)
                        .setDuration(300)
                        .withEndAction{
                            visibility = View.GONE
                        }
                        .start()
                }
            }
        }
    }

    private fun formIsValid(): Boolean {
        val newTitle = binding.examTitle.text
        val newSubtitle = binding.examSubtitle.text
        return (newTitle.isNotEmpty() && newSubtitle.isNotEmpty()) && (newTitle != examTextTitle || newSubtitle != examTextSubtitle)
    }

    companion object {
        const val ARG_EXAM_ID = "exam-id"
        const val ARG_EXAM_TITLE = "exam-title"
        const val ARG_EXAM_SUBTITLE = "exam-subtitle"
    }

    override fun onGlobalLayout() {
        try {
            binding.apply {
                val rect = Rect()
                root.getWindowVisibleDisplayFrame(rect)
                val screenHeight = binding.root.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    binding.saveButton.translationY = -keypadHeight.toFloat()
                } else {
                    binding.saveButton.translationY = 0f
                }
            }
        } catch (_: Throwable) {
            // silent is perfect
        }
    }

}