package com.il4mb.edudoexam.ui.dashboard.exams.detail.config

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.ExamsEndpoints
import com.il4mb.edudoexam.api.payloads.ExamPayload
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.ParticipantDiffCallback
import com.il4mb.edudoexam.components.Utils.Companion.asFormattedString
import com.il4mb.edudoexam.components.Utils.Companion.asLocalDateTime
import com.il4mb.edudoexam.components.Utils.Companion.copyTextToClipboard
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.Utils.Companion.getColor
import com.il4mb.edudoexam.components.Utils.Companion.toDate
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.components.ui.BaseFragment
import com.il4mb.edudoexam.components.ui.FloatingMenu
import com.il4mb.edudoexam.databinding.FragmentExamDetailConfigBinding
import com.il4mb.edudoexam.databinding.ViewItemUserBinding
import com.il4mb.edudoexam.models.Participant
import com.il4mb.edudoexam.models.User
import com.il4mb.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

class ExamConfigFragment : BaseFragment<FragmentExamDetailConfigBinding>(FragmentExamDetailConfigBinding::class.java),
    GenericListAdapter.ItemBindListener<Participant, ViewItemUserBinding> {

    private val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private var examTextTitle = ""
    private var examTextSubtitle = ""
    private var examId: String? = null
    private val listAdapter: GenericListAdapter<Participant, ViewItemUserBinding> by lazy {
        GenericListAdapter(
            ViewItemUserBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ParticipantDiffCallback()
        )
    }
    private val viewModel: DetailExamViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liveCycleObserve()
        setupUI()
    }

    private fun fetchBlockedUsers(examId: String) {
        viewModel.fetchBlockedParticipants(requireActivity(), examId)
    }

    private var startDate: LocalDateTime = LocalDateTime.now().plusHours(1)
        set(value) {
            field = value
            binding.examStartDate.text = field.asFormattedString
        }

    private var finishDate: LocalDateTime = LocalDateTime.now().plusHours(4)
        set(value) {
            field = value
            binding.examEndDate.text = field.asFormattedString
        }

    private fun liveCycleObserve() {

        viewModel.apply {
            exam.observe(viewLifecycleOwner) {
                examTextTitle    = it.title
                examTextSubtitle = it.subTitle
                examId           = it.id
                binding.apply {
                    examTitle.text     = it.title
                    examSubtitle.text  = it.subTitle
                    examCode.text      = it.id
                    startDate          = it.startAt.asLocalDateTime
                    finishDate         = it.finishAt.asLocalDateTime
                }
                fetchBlockedUsers(it.id)

                if(it.isOngoing) {
                    setupOngoingUI()
                } else if(it.finishAt.time < System.currentTimeMillis()) {
                    setupFinishedUI()

                } else {
                    binding.apply {
                        deleteExamButton.visibility = View.VISIBLE
                    }
                }
                validateForm()
            }
            blockedUsers.observe(viewLifecycleOwner) {
                if(it.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.blockedUsersRecycle.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.blockedUsersRecycle.visibility = View.VISIBLE
                }
                listAdapter.submitList(it)
            }
        }
    }

    private fun setupOngoingUI() {
        binding.apply {
            examTitle.isEnabled       = false
            examSubtitle.isEnabled    = false
            examStartDate.isClickable = false
            examEndDate.isClickable   = false
            ongoingInfoContainer.visibility = View.VISIBLE
            finishedInfoContainer.visibility  = View.GONE

            saveButton.visibility       = View.GONE
            deleteExamButton.visibility = View.GONE
            forceFinishExam.visibility  = View.VISIBLE
            forceStartExam.visibility   = View.GONE
        }
    }

    private fun setupFinishedUI() {
        binding.apply {
            examTitle.isEnabled       = false
            examSubtitle.isEnabled    = false
            examStartDate.isClickable = false
            examEndDate.isClickable   = false
            finishedInfoContainer.visibility = View.VISIBLE
            ongoingInfoContainer.visibility  = View.GONE
            deleteExamButton.visibility = View.VISIBLE
            saveButton.visibility       = View.GONE
            forceFinishExam.visibility  = View.GONE
            forceStartExam.visibility   = View.GONE
        }
    }

    private fun setupUI() {

        binding.apply {

            forceFinishExam.visibility = View.GONE
            forceStartExam.visibility = View.VISIBLE

            examTitle.apply {
                onTextChanged { validateForm() }
            }
            examSubtitle.apply {
                onTextChanged { validateForm() }
            }
            examCode.apply {
                onClickAtEnd {
                    if(copyTextToClipboard(requireContext(), examId.toString())) {
                        Toast.makeText(context, getString(R.string.exam_code_copied_to_clipboard), Toast.LENGTH_SHORT).show()
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
            examStartDate.onClickAtEnd {
                if(viewModel.exam.value?.isOngoing == false)
                    showDateTimePicker(LocalDateTime.now().plusMinutes(15)) {
                        startDate = it
                        finishDate = adjustFinishDate(startDate, finishDate)
                        validateForm()
                }
            }
            examEndDate.onClickAtEnd {
                if(viewModel.exam.value?.isOngoing == false)
                    showDateTimePicker(startDate.plusHours(3)) {
                        finishDate = it
                        validateForm()
                }
            }

            forceStartExam.setOnClickListener { handleForceStartExam() }
            forceFinishExam.setOnClickListener { handleForceFinishExam() }
            deleteExamButton.setOnClickListener { handleDeleteExam() }
        }
    }

    private fun showInfo(message: String) {
        InfoDialog(requireActivity())
            .setMessage(message)
            .show()
    }

    private fun showInfo(title: String, message: String) {
        InfoDialog(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .show()
    }


    private fun handleForceStartExam() {
        DialogBottom.Builder(requireActivity()).apply {
            title = getString(R.string.are_you_sure)
            message = "Are you sure you want to start this exam? This action cannot be undone."
            acceptText = "Start Exam"
            color = getColor(requireContext(), R.color.danger)
            acceptHandler = {
                sendForceRequest("start")
                setupOngoingUI()
                true
            }
        }.show()
    }
    private fun handleForceFinishExam() {
        DialogBottom.Builder(requireActivity()).apply {
            title = getString(R.string.are_you_sure)
            message = "Are you sure you want to finish this exam? This action cannot be undone."
            acceptText = "Finish Exam"
            color = getColor(requireContext(), R.color.danger)
            acceptHandler = {
                sendForceRequest("finish")
                setupFinishedUI()
                true
            }
        }.show()
    }
    private fun handleDeleteExam() {
        DialogBottom.Builder(requireActivity()).apply {
                title = getString(R.string.are_you_sure)
                message = getString(R.string.are_you_sure_you_want_to_delete_this_exam_this_action_cannot_be_undone)
                acceptText = getString(R.string.delete)
                color = getColor(requireContext(), R.color.danger)
                acceptHandler = {
                    sendDeleteRequest()
                    true
                }
            }.show()
    }
    private fun sendForceRequest(action: String) {
        viewModel.exam.value?.id?.let { examId ->
            Client<ExamsEndpoints, Response>(requireActivity(), ExamsEndpoints::class.java)
                .onError {
                    showErrorDialog(
                        title = "Failed force $action",
                        message = it.message,
                        textAction = "Retry",
                    ) { sendForceRequest(action) }
                }
                .onSuccess { success ->
                    showInfo("Successful", success.message)
                    fetchExam()
                }
                .fetch { it.toggleExam(examId, action) }
        } ?: run {
            showInfo(getString(R.string.missing_exam_id))
        }
    }

    private fun fetchExam() {
        viewModel.exam.value?.id?.let { examId ->
            viewModel.fetchExam(
                activity = requireActivity(),
                examId = examId,
                error = {
                    showErrorDialog(
                        title = "Failed fetch exam",
                        message = it.message,
                        textAction = "Retry",
                    ) { fetchExam() }
                }
            )
        }?: run {
            showInfo(getString(R.string.missing_exam_id))
        }
    }

    val fallbackDestinations = listOf(R.id.nav_histories, R.id.nav_exams, R.id.nav_home)

    private fun sendDeleteRequest() {
        viewModel.exam.value?.id?.let { examId ->
            Client<ExamsEndpoints, Response>(requireActivity(), ExamsEndpoints::class.java)
                .onError {
                    showErrorDialog(
                        title = "Failed to delete exam",
                        message = it.message,
                        textAction = "Retry"
                    ) { sendDeleteRequest() }
                }
                .onSuccess {
                    showInfo("Exam successfully deleted")
                    navigateBackToOriginOrFallback()
                }
                .fetch { it.deleteExam(examId) }
        } ?: run {
            showInfo(getString(R.string.missing_exam_id))
        }
    }

    // Helper function for handling navigation after deletion
    private fun navigateBackToOriginOrFallback() {
        val navController = findNavController()
        try {
            val previousDestinationId = navController.previousBackStackEntry?.destination?.id
            if (previousDestinationId != null && fallbackDestinations.contains(previousDestinationId)) {
                navController.popBackStack(previousDestinationId, false)
            } else {
                for (destination in fallbackDestinations) {
                    if (navController.popBackStack(destination, false)) {
                        return
                    }
                }
                showInfo(getString(R.string.navigation_failed))
            }
        } catch (e: Throwable) {
            showInfo("Unexpected navigation error: ${e.message}")
        }
    }


    private fun showErrorDialog(title: String, message: String, textAction: String?, handler: (() -> Unit) = {}) {
        DialogBottom.Builder(requireActivity()).apply {
            this.title = title
            this.message = message
            acceptText = textAction ?: ""
            acceptHandler = {
                handler()
                true
            }
            if(textAction.isNullOrEmpty()) {
                isAcceptActionButtonVisible = false
            }
        }.show()
    }

    private fun adjustFinishDate(startDate: LocalDateTime, finishDate: LocalDateTime): LocalDateTime {
        val hoursDifference = ChronoUnit.HOURS.between(startDate, finishDate)
        if (hoursDifference < 3) {
            return startDate.plusHours(3)
        }
        return finishDate
    }

    override fun onViewBind(binding: ViewItemUserBinding, item: Participant, position: Int) {

        binding.apply {

            userName.text = item.user.name
            userEmail.text = item.user.email
            if (item.user.photo != null) {
                Glide.with(requireContext())
                    .load(item.user.photo)
                    .into(userPhoto)
            } else {
                userPhoto.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (item.user.gender == 1) R.drawable.man else R.drawable.woman
                    )
                )
            }

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
                    setOnClickListener { showItemMenu(item.user, actionButton) }
                }
            }
        }
    }

    private fun showDateTimePicker(minDateTime: LocalDateTime? = null, onDateTimeSelected: (LocalDateTime) -> Unit) {
        val calendar = Calendar.getInstance()
        val minCalendar = Calendar.getInstance()

        minDateTime?.let {
            minCalendar.time = java.sql.Timestamp.valueOf(it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) // Convert LocalDateTime to Date
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        val selectedDateTime = LocalDateTime.of(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE)
                        )

                        if (minDateTime != null && selectedDateTime.isBefore(minDateTime)) {
                            Toast.makeText(
                                requireContext(),
                                getString(
                                    R.string.selected_date_and_time_must_be_after,
                                    minDateTime.format(dateTimeFormat)
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onDateTimeSelected(selectedDateTime)
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
        datePickerDialog.show()
    }

    private fun doUnblockUser(user: User) {
        examId?.let {
            viewModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    InfoDialog(requireActivity())
                        .setMessage(getString(R.string.removed_from_blocked_users, user.name))
                        .show()
                    viewModel.fetchBlockedParticipants(requireActivity(), examId!!)
                    //viewModel.withUsers(requireActivity()).fetch { it.getStudents(examId!!, false) }
                }
                .fetch { it.updateParticipant(examId!!, user.id, false) }
        }
    }

    private fun doSaveChanges() {
        examId?.let {
            val newTitle = binding.examTitle.text
            val newSubtitle = binding.examSubtitle.text
            viewModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    InfoDialog(requireActivity())
                        .setMessage(getString(R.string.exam_updated))
                        .show()
                    examTextTitle = newTitle
                    examTextSubtitle = newSubtitle
                    validateForm()
                }
                .fetch {
                    it.updateExam(
                        examId!!,
                        ExamPayload(
                            title = newTitle,
                            subTitle = newSubtitle,
                            startAt  = binding.examStartDate.text.toDate("yyyy-MM-dd HH:mm"),
                            finishAt = binding.examEndDate.text.toDate("yyyy-MM-dd HH:mm")
                        )
                    )
                }
        }
    }

    private fun showUnblockDialog(item: User) {
        DialogBottom.Builder(requireActivity()).apply {
            color = getColor(requireContext(), R.color.danger)
            title = getString(R.string.are_you_sure)
            message = getString(
                R.string.are_you_sure_you_want_to_unblock_user_from_this_exam_user_detail_name_email_this_action_cannot_be_undone,
                item.name,
                item.email
            )
            acceptText = getString(R.string.unblock)
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

            addItem(getString(R.string.remove_from_block)).apply {
                color = getColor(context, R.color.danger)
                setOnClickListener {
                    showUnblockDialog(item)
                    floatingMenu.hide()
                }
            }
        }.show()
    }

    private fun onErrorHandler(e: Response) {
        InfoDialog(requireActivity())
            .setTitle(getString(R.string.something_went_wrong))
            .setMessage(e.message)
            .show()
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
        val currentExam = viewModel.exam.value

        val isStartDateChanged = currentExam?.startAt?.asLocalDateTime != startDate
        val isFinishDateChanged = currentExam?.finishAt?.asLocalDateTime != finishDate

        val newTitle = binding.examTitle.text
        val newSubtitle = binding.examSubtitle.text

        val isTextFilled = newTitle.isNotEmpty() && newSubtitle.isNotEmpty()

        val isDataChanged = newTitle != examTextTitle || newSubtitle != examTextSubtitle || isStartDateChanged || isFinishDateChanged

        return isTextFilled && isDataChanged
    }

    companion object {
        const val ARG_EXAM_ID = "exam-id"
    }

}