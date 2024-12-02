package com.capstone.edudoexam.ui.dashboard.exams.detail.studens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.payloads.AddStudentPayload
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.ui.FloatingMenu
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.UserDiffCallback
import com.capstone.edudoexam.components.Utils
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getColor
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.databinding.FragmentStudentsExamBinding
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
    private val binding: FragmentStudentsExamBinding by lazy {
        FragmentStudentsExamBinding.inflate(layoutInflater)
    }
    private val viewModel: DetailExamViewModel by viewModels()
    private val genericAdapter:  GenericListAdapter<User, ViewItemUserBinding> by lazy {
        GenericListAdapter(
            ViewItemUserBinding::class.java,
            onItemBindCallback = this,
            diffCallback = UserDiffCallback()
        )
    }
    private var examId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        examId = arguments?.getString(ARG_EXAM_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericAdapter
            }
            floatingActionButton.setOnClickListener { addUserDialog() }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.apply {
            users.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    delay(400)
                    genericAdapter.submitList(it)
                    setLoading(false)
                }
            }
        }
        lifecycleScope.launch {
            delay(400)
            doFetchUsers()
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
                        .setTitle("Success")
                        .setMessage("Student added")
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
                        .setMessage("Student removed")
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
                        .setMessage("Student blocked")
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
                .setTitle("Something went wrong")
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

            addItem("Remove").apply {
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

            addItem("Block").apply {
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
            title = "Are you sure?"
            message = "Are you sure you want to block user from this exam?\nUser detail:\nName\t: ${item.name}\nEmail\t: ${item.email}\n"
            acceptText = "Block"
            acceptHandler = {
                doBlockUser(item.id)
                true
            }
        }.show()
    }

    private fun actionRemoveHandler(item: User) {

        DialogBottom.Builder(requireActivity()).apply {
            color = getColor(requireContext(), R.color.danger)
            title = "Are you sure?"
            message = "Are you sure you want to remove user from this exam?\nUser detail:\nName\t: ${item.name}\nEmail\t: ${item.email}\nThis action cannot be undone."
            acceptText = "Remove"
            acceptHandler = {
                doRemoveUser(item.id)
                true
            }
        }.show()

    }

    private fun addUserDialog() {
        DialogBottom.Builder(requireActivity()).apply {

            title   = "Add User"
            message = "Please enter email user, make sure user has ben registered."
            setLayout(bindingModalAddUser)
            acceptHandler = { modal ->
                val isValid = bindingModalAddUser.inputEmail.isValid
                if (!isValid) {
                    bindingModalAddUser.inputEmail.error = "Email is required"
                } else {
                    bindingModalAddUser.inputEmail.error = ""
                    doAddUser(bindingModalAddUser.inputEmail.text, modal)
                }

                false
            }

            acceptText = "Add"

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

    companion object {
        private const val ARG_EXAM_ID = "exam_id"

        fun newInstance(examId: String?): StudentsExamFragment {
            val fragment = StudentsExamFragment()
            val args = Bundle()
            args.putString(ARG_EXAM_ID, examId)
            fragment.arguments = args
            return fragment
        }
    }
}