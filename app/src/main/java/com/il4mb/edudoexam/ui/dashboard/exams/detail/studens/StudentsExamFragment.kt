package com.il4mb.edudoexam.ui.dashboard.exams.detail.studens

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.api.payloads.AddStudentPayload
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.components.GenericListAdapter
import com.il4mb.edudoexam.components.ParticipantDiffCallback
import com.il4mb.edudoexam.components.Utils
import com.il4mb.edudoexam.components.Utils.Companion.dp
import com.il4mb.edudoexam.components.Utils.Companion.getColor
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.components.dialog.InfoDialog
import com.il4mb.edudoexam.components.ui.FloatingMenu
import com.il4mb.edudoexam.components.ui.ResultCard
import com.il4mb.edudoexam.components.ui.SummaryLayout
import com.il4mb.edudoexam.databinding.FragmentExamDetailStudentsBinding
import com.il4mb.edudoexam.databinding.ViewItemUserBinding
import com.il4mb.edudoexam.databinding.ViewItemUserSummaryBinding
import com.il4mb.edudoexam.databinding.ViewModalAddUserBinding
import com.il4mb.edudoexam.models.AnswerContainer
import com.il4mb.edudoexam.models.Participant
import com.il4mb.edudoexam.models.Question
import com.il4mb.edudoexam.models.User
import com.il4mb.edudoexam.ui.dashboard.exams.detail.DetailExamViewModel
import com.il4mb.edudoexam.ui.dashboard.histories.student.StudentResultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class StudentsExamFragment: Fragment(),
    GenericListAdapter.ItemBindListener<Participant, ViewItemUserBinding>{

    private val emotionIcons = mapOf(
        "Anger" to R.drawable.emo_angry,
        "Fear" to R.drawable.emo_fear,
        "Surprised" to R.drawable.emo_shocked,
        "Happy" to R.drawable.emo_smiling,
        "Sad" to R.drawable.emo_sad,
        "Disgust" to R.drawable.emo_tired,
        "Neutral" to R.drawable.emo_neutral
    )

    private val bindingModalAddUser: ViewModalAddUserBinding by lazy {
        ViewModalAddUserBinding.inflate(layoutInflater)
    }
    private val binding: FragmentExamDetailStudentsBinding by lazy {
        FragmentExamDetailStudentsBinding.inflate(layoutInflater)
    }
    private var examId: String? = null
    private val liveModel: DetailExamViewModel by activityViewModels()
    private val genericAdapter:  GenericListAdapter<Participant, ViewItemUserBinding> by lazy {
        GenericListAdapter(
            ViewItemUserBinding::class.java,
            onItemBindCallback = this,
            diffCallback = ParticipantDiffCallback()
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        liveCycleObserve()
        lifecycleScope.launch {
            delay(400)
            doFetchUsers()
        }
    }

    override fun onResume() {
        super.onResume()
        setupUI()
        doFetchUsers()
    }

    private fun setupUI() {
        binding.apply {
            emptyState.visibility = View.GONE
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = genericAdapter
            }
            floatingActionButton.setOnClickListener { addUserDialog() }
        }
    }

    private fun liveCycleObserve() {
        liveModel.apply {
            participants.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    binding.apply {
                        emptyState.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }

                    genericAdapter.submitList(it)
                }
            }
            exam.observe(viewLifecycleOwner) {
                examId = it.id
                if(it.finishAt.time < System.currentTimeMillis()) {
                    binding.floatingActionButton.visibility = View.GONE
                }
            }
        }
    }

    fun doFetchUsers() {
        liveModel.exam.value?.id?.let { examId ->
            liveModel.fetchParticipants(requireActivity(), examId)
        } ?: run {
            showInfo(getString(R.string.missing_exam_id))
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

    private fun doAddUser(email: String, modal: DialogBottom) {
        Utils.hideKeyboard(requireActivity())
        modal.dismiss()
        examId?.let { id ->
            liveModel.withNoResult(requireActivity())
                .onError {
                    onErrorHandler(it)
                    lifecycleScope.launch {
                        delay(400)
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
                    showInfo(getString(R.string.success), getString(R.string.student_added))
                }
                .fetch { it.addParticipant(id, AddStudentPayload(email)) }
        }
    }

    private fun doRemoveUser(uid: String) {
        examId?.let { examId ->
            liveModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    doFetchUsers()
                    showInfo(getString(R.string.student_removed))
                }
                .fetch { it.removeParticipant(examId, uid) }
        }
    }

    private fun doBlockParticipant(uid: String) {
        examId?.let { examId ->
            liveModel.withNoResult(requireActivity())
                .onError { onErrorHandler(it) }
                .onSuccess {
                    doFetchUsers()
                    InfoDialog(requireActivity())
                        .setMessage(getString(R.string.student_blocked))
                        .show()
                }
                .fetch { it.updateParticipant(examId, uid, true) }
        }
    }

    private fun onErrorHandler(e: Response) {
        InfoDialog(requireActivity())
            .setTitle(getString(R.string.something_went_wrong))
            .setMessage(e.message)
            .show()
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
                doBlockParticipant(item.id)
                true
            }
        }.show()
    }

    @SuppressLint("StringFormatInvalid")
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

    private val userSummaryBinding: ViewItemUserSummaryBinding by lazy {
        ViewItemUserSummaryBinding.inflate(layoutInflater)
    }

    private fun updateSummaryUI(layout: SummaryLayout, summaries: MutableMap<String, Int>?) {
        val maxValue = summaries?.values?.sum() ?: 0
        binding.apply {
            layout.removeAllViews()
            summaries?.forEach { (key, value) ->
                val icon = emotionIcons[key]
                val percentage = if (maxValue > 0) (value * 100) / maxValue else 0
                if (icon != null && percentage > 20) {
                    layout.addItem(icon, percentage)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun userSummaryDialog(participant: Participant) {

        val questions = liveModel.questions.value?: mutableListOf()
        val questionsLength = questions.size
        val wrongLength   = participant.answer?.getTotalWrong(questions) ?: 0
        val correctLength = participant.answer?.getTotalCorrect(questions) ?: 0

        userSummaryBinding.apply {

            animateTextView(questionLengthCard, questionsLength, 600)
            animateTextView(correctAnswerCard, correctLength, 600)
            animateTextView(wrongAnswerCard, wrongLength, 600)

            userCard.apply {
                actionButton.visibility = View.GONE
                userName.text = participant.user.name
                userEmail.text = participant.user.email
                if(participant.user.photo != null) {
                    Glide.with(requireContext())
                        .load(participant.user.photo)
                        .into(userPhoto)
                }
                else {
                    userPhoto.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (participant.user.gender == 1) R.drawable.man else R.drawable.woman
                        )
                    )
                }

                scoreTextView.apply {
                    visibility = View.VISIBLE
                    text = "${getScore(participant.answer)}%"
                }
            }

            updateSummaryUI(summaryLayout, participant.answer?.summaryAccumulation())

        }

        DialogBottom.Builder(requireActivity())
            .apply {
                title = "Summary of \"${participant.user.name}\""
                view = userSummaryBinding.root
                acceptText = "Read more"
                acceptHandler = {
                    navigateToStudentResult(questions, participant)
                    true
                }
            }.show()
    }
    private val studentResultLiveData: StudentResultViewModel by activityViewModels()
    private fun navigateToStudentResult(questions: MutableList<Question>, participant: Participant) {
        studentResultLiveData.prepare(liveModel.exam.value!!, questions, participant).also {
            findNavController().navigate(R.id.action_nav_exam_detail_to_nav_student_result)
        }
    }
    private fun getScore(answer: AnswerContainer?): Int {
        val questions = liveModel.questions.value ?: mutableListOf()
        val totalCorrect = answer?.getTotalCorrect(questions) ?: 0
        val questionLength = questions.size
        if (questionLength == 0) return 0

        return ((totalCorrect.toDouble() / questionLength) * 100).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun setupAnswered(binding: ViewItemUserBinding, item: Participant) {
        binding.apply {
            scoreTextView.apply {
                visibility = View.VISIBLE
                text = "${getScore(item.answer)}%"
            }

            root.apply {
                setOnClickListener {
                    userSummaryDialog(item)
                }
                strokeColor = context.getColor(R.color.primary_light)
                strokeWidth = 1.dp
            }
        }
    }

    private fun setupNotAnswered(binding: ViewItemUserBinding, item: Participant) {
        binding.apply {
            scoreTextView.visibility = View.GONE
        }
    }

    override fun onViewBind(binding: ViewItemUserBinding, item: Participant, position: Int) {

        binding.apply {

            item.user.let { user ->
                userName.text  = user.name
                userEmail.text = user.email
                if(user.photo != null) {
                    Glide.with(requireContext())
                        .load(user.photo)
                        .into(userPhoto)
                }
                else {
                    userPhoto.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (user.gender == 1) R.drawable.man else R.drawable.woman
                        )
                    )
                }
            }


            actionButton.apply {
                liveModel.exam.value?.let { exam ->
                    if(exam.isOngoing) {
                        visibility = View.GONE
                    } else if(exam.finishAt.time < System.currentTimeMillis()) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        setOnClickListener { showItemMenu(item.user, actionButton) }
                    }
                }
            }

            item.answer?.let { setupAnswered(binding, item) } ?: setupNotAnswered(binding, item)
        }
    }

    private fun animateTextView(textView: ResultCard, endValue: Int, duration: Long = 600L) {
        val valueAnimator = ValueAnimator.ofInt(0, endValue)
        valueAnimator.duration = duration
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()

        valueAnimator.addUpdateListener { animator ->
            textView.score = animator.animatedValue.toString()
        }

        valueAnimator.start()
    }

    companion object {

        fun newInstance(): StudentsExamFragment {
            val fragment = StudentsExamFragment()
            return fragment
        }
    }


}