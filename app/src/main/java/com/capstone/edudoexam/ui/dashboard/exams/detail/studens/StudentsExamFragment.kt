package com.capstone.edudoexam.ui.dashboard.exams.detail.studens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.payloads.AddStudentPayload
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.UserDiffCallback
import com.capstone.edudoexam.components.Utils
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getColor
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.components.ui.FloatingMenu
import com.capstone.edudoexam.databinding.FragmentExamDetailStudentsBinding
import com.capstone.edudoexam.databinding.ViewItemUserBinding
import com.capstone.edudoexam.databinding.ViewModalAddUserBinding
import com.capstone.edudoexam.models.User
import com.capstone.edudoexam.ui.dashboard.DashboardActivity
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudentsExamFragment: Fragment(),
    GenericListAdapter.ItemBindListener<User, ViewItemUserBinding> {
    private val bindingModalAddUser: ViewModalAddUserBinding by lazy {
        ViewModalAddUserBinding.inflate(layoutInflater)
    }
    private val binding: FragmentExamDetailStudentsBinding by lazy {
        FragmentExamDetailStudentsBinding.inflate(layoutInflater)
    }
    private var examId: String? = null
    private val viewModel: DetailExamViewModel by activityViewModels()
    private val genericAdapter:  GenericListAdapter<User, ViewItemUserBinding> by lazy {
        GenericListAdapter(
            ViewItemUserBinding::class.java,
            onItemBindCallback = this,
            diffCallback = UserDiffCallback()
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveCycleObserve()
        setupUI()

        lifecycleScope.launch {
            delay(400)
            doFetchUsers()
        }
    }

    private fun setupUI() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericAdapter
            }
            floatingActionButton.setOnClickListener { addUserDialog() }
        }
    }

    private fun liveCycleObserve() {
        viewModel.apply {
            users.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    delay(600)

                    if(it.isEmpty()) {
                        binding.apply {
                            emptyState.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        }
                    } else {
                        binding.apply {
                            emptyState.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                    genericAdapter.submitList(it)
                    setLoading(false)
                }
            }
            exam.observe(viewLifecycleOwner) {
                examId = it.id
                if(it.isOngoing) {
                    binding.floatingActionButton.visibility = View.GONE
                }
            }
        }
    }

    fun doFetchUsers() {
        examId?.let { examId ->
            setLoading(true)
            viewModel.withUsers(requireActivity())
                .onError { onErrorHandler(it) }
                .fetch { it.getStudents(examId,false) }
        }
    }

    private fun doAddUser(email: String, modal: DialogBottom) {
        Utils.hideKeyboard(requireActivity())
        modal.dismiss()
        examId?.let { id ->
            setLoading(true)
            viewModel.withNoResult(requireActivity())
                .onError {
                    onErrorHandler(it)
                    lifecycleScope.launch {
                        delay(800)
                        addUserDialog()
                    }
                }
                .onSuccess {
                    bindingModalAddUser.inputEmail.apply {
                        text = ""
                        error = ""
                    }
                    modal.dismissNow()
                    doFetchUsers()
                    InfoDialog(requireActivity())
                        .setTitle(getString(R.string.success))
                        .setMessage(getString(R.string.student_added))
                        .show()
                }
                .fetch { it.addStudent(id, AddStudentPayload(email)) }
        }
    }

    private fun doRemoveUser(uid: String) {
        examId?.let { examId ->
            setLoading(true)
            viewModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    doFetchUsers()
                    InfoDialog(requireActivity())
                        .setMessage(getString(R.string.student_removed))
                        .show()
                }
                .fetch { it.removeStudent(examId, uid) }
        }
    }

    private fun doBlockUser(uid: String,) {
        examId?.let { examId ->
            setLoading(true)
            viewModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    doFetchUsers()
                    InfoDialog(requireActivity())
                        .setMessage(getString(R.string.student_blocked))
                        .show()
                }
                .fetch { it.updateStudent(examId, uid, true) }
        }
    }

    private fun onErrorHandler(e: Response) {
        lifecycleScope.launch {
            delay(400)
            setLoading(false)
            InfoDialog(requireActivity())
                .setTitle(getString(R.string.something_went_wrong))
                .setMessage(e.message)
                .show()
        }
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

            addItem(getString(R.string.remove)).apply {
                icon = ContextCompat.getDrawable(
                    context,
                    R.drawable.baseline_person_remove_24
                )
                color = getColor(context, R.color.danger)
                setOnClickListener {
                    floatingMenu.hide()
                    actionRemoveHandler(item)
                }
            }

            addItem(getString(R.string.block)).apply {
                icon = ContextCompat.getDrawable(
                    context,
                    R.drawable.baseline_remove_circle_24
                )
                color = getColor(context, R.color.waring)
                setOnClickListener {
                    floatingMenu.hide()
                    actionBlockHandler(item)
                }
            }
        }.show()
    }

    private fun actionBlockHandler(item: User) {
        DialogBottom.Builder(requireActivity()).apply {
            color = getColor(requireContext(), R.color.waring)
            title = getString(R.string.are_you_sure)
            message = getString(
                R.string.are_you_sure_you_want_to_block_user_from_this_exam_user_detail_name_email,
                item.name,
                item.email
            )
            acceptText = getString(R.string.block)
            acceptHandler = {
                doBlockUser(item.id)
                true
            }
        }.show()
    }

    private fun actionRemoveHandler(item: User) {

        DialogBottom.Builder(requireActivity()).apply {
            color = getColor(requireContext(), R.color.danger)
            title = getString(R.string.are_you_sure)
            message = getString(
                R.string.are_you_sure_you_want_to_remove_user_from_this_exam_user_detail_name_email_this_action_cannot_be_undone,
                item.name,
                item.email
            )
            acceptText = getString(R.string.remove)
            acceptHandler = {
                doRemoveUser(item.id)
                true
            }
        }.show()

    }

    private fun addUserDialog() {
        DialogBottom.Builder(requireActivity()).apply {

            title   = getString(R.string.add_user)
            message = getString(R.string.please_enter_email_user_make_sure_user_has_ben_registered)
            view    = bindingModalAddUser.root
            acceptHandler = { modal ->
                val isValid = bindingModalAddUser.inputEmail.isValid
                if (!isValid) {
                    bindingModalAddUser.inputEmail.error = getString(R.string.email_is_required)
                } else {
                    bindingModalAddUser.inputEmail.error = ""
                    doAddUser(bindingModalAddUser.inputEmail.text, modal)
                }

                false
            }

            acceptText = getString(R.string.add)

        }.show()
    }

    private fun setLoading(isLoading: Boolean) {
        (requireActivity() is DashboardActivity).apply {
            (requireActivity() as DashboardActivity).setLoading(isLoading)
        }
    }

    override fun onViewBind(binding: ViewItemUserBinding, item: User, position: Int) {

        binding.apply {
            userName.text = item.name
            userEmail.text = item.email
            userPhoto.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), if(item.gender == 1) R.drawable.man else R.drawable.woman)
            )



            root.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 18.dp)
                    layoutParams = this
                }
                actionButton.apply {
                    if(viewModel.exam.value?.isOngoing == true) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        setOnClickListener { showItemMenu(item, actionButton) }
                    }
                }

            }
        }
    }

    companion object {

        fun newInstance(): StudentsExamFragment {
            val fragment = StudentsExamFragment()
            return fragment
        }
    }
}