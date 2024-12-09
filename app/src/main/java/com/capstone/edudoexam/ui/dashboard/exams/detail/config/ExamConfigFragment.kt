package com.capstone.edudoexam.ui.dashboard.exams.detail.config

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.payloads.ExamPayload
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.UserDiffCallback
import com.capstone.edudoexam.components.Utils.Companion.asFormattedString
import com.capstone.edudoexam.components.Utils.Companion.asLocalDateTime
import com.capstone.edudoexam.components.Utils.Companion.copyTextToClipboard
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getColor
import com.capstone.edudoexam.components.Utils.Companion.toDate
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.components.ui.FloatingMenu
import com.capstone.edudoexam.databinding.FragmentExamDetailConfigBinding
import com.capstone.edudoexam.databinding.ViewItemUserBinding
import com.capstone.edudoexam.models.User
import com.capstone.edudoexam.ui.dashboard.exams.ExamsViewModel
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExamConfigFragment : BaseFragment<FragmentExamDetailConfigBinding>(FragmentExamDetailConfigBinding::class.java),
    GenericListAdapter.ItemBindListener<User, ViewItemUserBinding>,
    ViewTreeObserver.OnGlobalLayoutListener {

    private val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private var examTextTitle = ""
    private var examTextSubtitle = ""
    private var examId: String? = null
    private val listAdapter: GenericListAdapter<User, ViewItemUserBinding> by lazy {
        GenericListAdapter(
            ViewItemUserBinding::class.java,
            onItemBindCallback = this,
            diffCallback = UserDiffCallback()
        )
    }
    private val viewModel: DetailExamViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liveCycleObserve()
        setupUI()
    }

    private fun fetchBlockedUsers(examId: String) {
        viewModel.fetchBlockedUsers(requireActivity(), examId)
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
                lifecycleScope.launch {
                    delay(800)
                    setLoading(false)
                }
            }
        }
    }

    private fun setupOngoingUI() {
        binding.apply {
            examTitle.isEnabled = false
            examSubtitle.isEnabled = false
            saveButton.visibility = View.GONE
            examStartDate.isClickable = false
            examEndDate.isClickable = false
            ongoingWarningInfoContainer.visibility = View.VISIBLE
        }
    }

    private fun setupUI() {

        binding.apply {

            root.viewTreeObserver.addOnGlobalLayoutListener(this@ExamConfigFragment)

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
        }
    }

    private fun adjustFinishDate(startDate: LocalDateTime, finishDate: LocalDateTime): LocalDateTime {
        val hoursDifference = ChronoUnit.HOURS.between(startDate, finishDate)
        if (hoursDifference < 3) {
            return startDate.plusHours(3)
        }
        return finishDate
    }

    override fun onViewBind(binding: ViewItemUserBinding, item: User, position: Int) {

        binding.apply {

            userName.text  = item.name
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
                    visibility = View.VISIBLE
                    setOnClickListener { showItemMenu(item, actionButton) }
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
            setLoading(true)
            viewModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    InfoDialog(requireActivity())
                        .setMessage(getString(R.string.removed_from_blocked_users, user.name))
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
                        .setMessage(getString(R.string.exam_updated))
                        .show()
                    setLoading(false)
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
        lifecycleScope.launch {
            delay(400)
            setLoading(false)
            InfoDialog(requireActivity())
                .setTitle(getString(R.string.something_went_wrong))
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
        val currentExam = viewModel.exam.value

        // Check if start and finish dates are different
        val isStartDateChanged = currentExam?.startAt?.asLocalDateTime != startDate
        val isFinishDateChanged = currentExam?.finishAt?.asLocalDateTime != finishDate

        // Get new input values
        val newTitle = binding.examTitle.text
        val newSubtitle = binding.examSubtitle.text

        // Check if the title and subtitle are filled
        val isTextFilled = newTitle.isNotEmpty() && newSubtitle.isNotEmpty()

        // Check if any values have changed
        val isDataChanged = newTitle != examTextTitle || newSubtitle != examTextSubtitle || isStartDateChanged || isFinishDateChanged

        // Form is valid if text is filled and data has changed
        return isTextFilled && isDataChanged
    }

    companion object {
        const val ARG_EXAM_ID = "exam-id"
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