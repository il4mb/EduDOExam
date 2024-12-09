package com.capstone.edudoexam.ui.dashboard.exams

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.edudoexam.R
import com.capstone.edudoexam.api.Client
import com.capstone.edudoexam.api.ExamsEndpoints
import com.capstone.edudoexam.api.payloads.ExamPayload
import com.capstone.edudoexam.api.response.Response
import com.capstone.edudoexam.components.ExamDiffCallback
import com.capstone.edudoexam.components.GenericListAdapter
import com.capstone.edudoexam.components.Snackbar
import com.capstone.edudoexam.components.Utils
import com.capstone.edudoexam.components.Utils.Companion.asEstimateTime
import com.capstone.edudoexam.components.Utils.Companion.asFormattedString
import com.capstone.edudoexam.components.Utils.Companion.asLocalDateTime
import com.capstone.edudoexam.components.Utils.Companion.dp
import com.capstone.edudoexam.components.Utils.Companion.getAttr
import com.capstone.edudoexam.components.Utils.Companion.hideKeyboard
import com.capstone.edudoexam.components.Utils.Companion.toDate
import com.capstone.edudoexam.components.dialog.DialogBottom
import com.capstone.edudoexam.components.dialog.InfoDialog
import com.capstone.edudoexam.components.input.InputTextEdit
import com.capstone.edudoexam.components.ui.BaseFragment
import com.capstone.edudoexam.databinding.FragmentExamsBinding
import com.capstone.edudoexam.databinding.ViewItemExamBinding
import com.capstone.edudoexam.databinding.ViewModalCreateExamBinding
import com.capstone.edudoexam.databinding.ViewPopupLayoutBinding
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import com.capstone.edudoexam.ui.dashboard.histories.student.ExamStudentResultFragment.Companion.EXAM_ID
import com.capstone.edudoexam.ui.exam.ExamActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

class ExamsFragment :
    BaseFragment<FragmentExamsBinding>(FragmentExamsBinding::class.java),
    GenericListAdapter.ItemBindListener<Exam, ViewItemExamBinding> {
    private val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
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
    private val viewModel: ExamsViewModel by activityViewModels()

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

                binding.apply {
                    if(it.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                    } else {
                        emptyState.visibility = View.GONE
                    }
                }
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

    @SuppressLint("SetTextI18n")
    override fun onViewBind(binding: ViewItemExamBinding, item: Exam, position: Int) {
        binding.apply {
            codeTextView.text = item.id
            titleView.text = item.title
            subtitleView.text = item.subTitle
            if(item.isAnswered) {
                dateTime.apply {
                    text = getString(R.string.answered)
                    setTextColor(context.getColor(R.color.primary_200))
                    typeface = resources.getFont(R.font.montserrat_bold)
                }
                deckCard.visibility = View.GONE
            } else if(item.isOngoing) {
                ongoingBadged.visibility = View.VISIBLE
                dateTime.apply {
                    text = getString(R.string.ongoing)
                    setTextColor(context.getColor(R.color.secondary))
                    typeface = resources.getFont(R.font.montserrat_bold)
                }
                deckCard.visibility = View.GONE
            } else {
                dateTime.apply {
                    text = getString(R.string.starting_in, item.startAt.asLocalDateTime.asEstimateTime)
                    setTextColor(context.getColor(R.color.secondary))
                }
            }
            deckCard.setOnClickListener {
                if(Utils.copyTextToClipboard(requireContext(), item.id)) {
                    showInfo("Exam Code Copied to Clipboard")
                }
            }
            if(item.isOwner) {
                teacherLabel.visibility = View.VISIBLE
            } else {
                studentLabel.visibility = View.VISIBLE
            }

            root.apply {
                (layoutParams as MarginLayoutParams).let {
                    it.bottomMargin = 35
                    it.marginStart = 14.dp
                    it.marginEnd = 14.dp
                    root.layoutParams = it
                }
                setOnClickListener {
                    if(item.isOwner) {
                        openDetailExamForTeacher(item)
                    } else {
                        if(item.isAnswered) {
                            openDetailExamForStudent(item)
                        } else {
                            openExam(item)
                        }
                    }
                }
            }
        }
    }

    private fun fetchExams() {
        setLoading(true)
        viewModel.withExams(requireActivity())
            .onError {
                setLoading(false)
                if(it.code == 404) {
                    listAdapter.submitList(listOf())
                } else {
                    lifecycleScope.launch {
                        delay(600)
                        Snackbar.with(binding.root)
                            .show(getString(R.string.something_went_wrong), it.message, Snackbar.LENGTH_LONG)
                    }
                }
            }
            .fetch { it.getExams() }
    }

    private val detailExamViewModel: DetailExamViewModel by activityViewModels()
    private fun openDetailExamForStudent(exam: Exam) {
        findNavController().navigate(R.id.action_nav_exams_to_nav_student_result, Bundle().apply {
            putString(EXAM_ID, exam.id)
        })
    }
    private fun openDetailExamForTeacher(exam: Exam) {
        detailExamViewModel.setExam(exam).also {
            findNavController().navigate(R.id.action_nav_exams_to_nav_exam_detail)
        }
    }
    private fun openExam(exam: Exam) {
        if(!exam.isOngoing) {
            showInfo(getString(R.string.this_exam_has_not_started_yet))
            return
        }
        startActivity(Intent(requireActivity(), ExamActivity::class.java).apply {
            putExtra("exam-id", exam.id)
        })
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

    private val createModalBinding: ViewModalCreateExamBinding by lazy {

        var startDate: LocalDateTime = LocalDateTime.now().plusHours(1)
        var finishDate: LocalDateTime = LocalDateTime.now().plusHours(4)

        fun calculateDuration(startDate: LocalDateTime, endDate: LocalDateTime): String {
            val years = ChronoUnit.YEARS.between(startDate, endDate)
            val months = ChronoUnit.MONTHS.between(startDate.plusYears(years), endDate)
            val days = ChronoUnit.DAYS.between(startDate.plusYears(years).plusMonths(months), endDate)
            val hours = ChronoUnit.HOURS.between(startDate.plusYears(years).plusMonths(months).plusDays(days), endDate)
            val minutes = ChronoUnit.MINUTES.between(startDate.plusYears(years).plusMonths(months).plusDays(days).plusHours(hours), endDate)

            return buildString {
                if (years > 0) append("$years years ")
                if (months > 0) append("$months months ")
                if (days > 0) append("$days days ")
                if (hours > 0) append("$hours hours ")
                if (minutes > 0) append("$minutes minutes")
                if (isBlank()) append("Less than a minute")
            }.trim()
        }
        fun showDateTimePicker(minDateTime: LocalDateTime? = null, onDateTimeSelected: (LocalDateTime) -> Unit) {
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
                                    getString(R.string.selected_date_and_time_must_be_after, minDateTime.format(dateTimeFormat)),
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
        fun adjustFinishDate(startDate: LocalDateTime, finishDate: LocalDateTime): LocalDateTime {
            val hoursDifference = ChronoUnit.HOURS.between(startDate, finishDate)
            if (hoursDifference < 3) {
                return startDate.plusHours(3)
            }
            return finishDate
        }

        ViewModalCreateExamBinding.inflate(layoutInflater).apply {

            examStartDate.apply {
                text = startDate.format(dateTimeFormat)
                helperText = buildString {
                    append(context.getString(R.string.starting_at))
                    append(calculateDuration(LocalDateTime.now(), startDate))
                }
                onClickAtEnd {
                    showDateTimePicker { selectedDateTime ->
                        startDate = selectedDateTime
                        text = startDate.format(dateTimeFormat)
                        helperText = buildString {
                            append(context.getString(R.string.starting_at))
                            append(calculateDuration(LocalDateTime.now(), startDate))
                        }

                        // Adjust finish date if needed
                        finishDate = adjustFinishDate(startDate, finishDate)
                        examFinishDate.apply {
                            text = finishDate.format(dateTimeFormat)
                            helperText = buildString {
                                append("Duration ")
                                append(calculateDuration(startDate, finishDate))
                            }
                        }
                    }
                }
            }

            examFinishDate.apply {
                text = finishDate.format(dateTimeFormat)
                helperText = buildString {
                    append("Duration ")
                    append(calculateDuration(startDate, finishDate))
                }
                onClickAtEnd {
                    showDateTimePicker(startDate.plusHours(3)) { selectedDateTime ->
                        finishDate = selectedDateTime
                        text = finishDate.format(dateTimeFormat)
                        helperText = buildString {
                            append("Duration ")
                            append(calculateDuration(startDate, finishDate))
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun createExam() {
        DialogBottom.Builder(requireActivity()).apply {
            title = getString(R.string.create_exam)
            view  = createModalBinding.root
            acceptText = getString(R.string.create)
            acceptHandler = {
                submitNewExamForm()
                true
            }
            dismissHandler = {
                createModalBinding.apply {
                    examTitle.text = ""
                    examSubtitle.text = ""
                }
                true
            }
        }.show()
    }

    private fun submitNewExamForm() {
        val title    = createModalBinding.examTitle.text
        val subtitle = createModalBinding.examSubtitle.text
        val startAt  = createModalBinding.examStartDate.text.toDate("yyyy-MM-dd HH:mm")
        val finishAt = createModalBinding.examFinishDate.text.toDate("yyyy-MM-dd HH:mm")
        Client<ExamsEndpoints, Response>(requireActivity(), ExamsEndpoints::class.java)
            .onError {
                createExam()
                showInfo(it.message)
            }
            .onSuccess {
                showInfo(getString(R.string.exam_created_successful)).also {
                    createModalBinding.apply {
                        examTitle.text = ""
                        examSubtitle.text = ""
                        examStartDate.text = ""
                        examFinishDate.text = ""
                    }
                    fetchExams()
                }
            }
            .fetch { it.addExam(ExamPayload(title, subtitle, startAt, finishAt)) }
    }

    private fun submitJoinExam(code: String) {
        hideKeyboard(requireActivity())
        setLoading(true)
        Client<ExamsEndpoints, Response>(requireActivity(), ExamsEndpoints::class.java)
            .onError {
                lifecycleScope.launch {
                    showInfo(it.message)
                    delay(400)
                    setLoading(false)
                }
            }
            .onSuccess {
                showInfo("Successful")
                lifecycleScope.launch {
                    delay(400)
                    setLoading(false)
                    fetchExams()
                }
            }
            .fetch { it.joinExam(code) }
    }

    private fun joinExam() {
        DialogBottom.Builder(requireActivity()).apply {
            title = getString(R.string.join_exam)
            view  = InputTextEdit(requireContext()).apply {
                hint = "Enter Exam Code"
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).let {
                    it.topMargin = 14.dp
                    it.bottomMargin = 18.dp
                    layoutParams = it
                }
            }
            acceptText = getString(R.string.join)
            acceptHandler = {
                submitJoinExam((view as InputTextEdit).text)
                true
            }
            dismissHandler = {
                createModalBinding.apply {
                    examTitle.text = ""
                    examSubtitle.text = ""
                }
                true
            }
        }.show()
    }

    private fun showInfo(message: String) {
        InfoDialog(requireActivity())
            .setMessage(message)
            .show()
    }

    override fun onResume() {
        super.onResume()
        fetchExams()
    }
}