package com.il4mb.edudoexam.ui.exam

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.il4mb.edudoexam.R
import com.il4mb.edudoexam.components.Utils.Companion.getAttr
import com.il4mb.edudoexam.components.dialog.DialogBottom
import com.il4mb.edudoexam.database.AppDatabase
import com.il4mb.edudoexam.databinding.ActivityExamBinding
import com.il4mb.edudoexam.ui.exam.question.QuestionFragment

interface ExamHelper {
    fun showErrorMessage(title: String, message: String, actionText: String, actionCallback: () -> Unit)
    fun getExamId(): String
}
class ExamActivity : AppCompatActivity(), QuestionFragment.QuestionHelper, ExamHelper {

    private var examId: String = ""
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityExamBinding
    private val questionController: QuestionsController by lazy {
        QuestionsController(binding.questionsNodeLayout)
    }
    private val liveModel: ExamViewModel by viewModels()
    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        examId = intent.getStringExtra(EXAM_ID) ?: ""
        if(examId.isEmpty()) {
            showErrorMessage("Something went wrong", "Missing exam id", "Exit") {
                finish()
            }
        }

        binding = ActivityExamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_exam)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.nav_question -> {
                    binding.apply {
                        drawerLayout.isEnabled = true
                        appBarExam.appBarLayout.visibility = View.VISIBLE
                    }
                }
                else -> {
                    binding.apply {
                        drawerLayout.isEnabled = false
                        appBarExam.appBarLayout.visibility = View.GONE
                    }
                }
            }
        }
        liveModel.apply {
            setDatabase(appDatabase)
            exam.observe(this@ExamActivity) { exam ->
                binding.apply {
                    examTitle.text    = exam.title
                    examSubtitle.text = exam.subTitle
                    examCode.text     = exam.id
                }
            }
            questions.observe(this@ExamActivity) {
                binding.questionsNodeLayout.nodeLength = it.size
            }
            currentQuestion.observe(this@ExamActivity) {
                val index = questions.value?.indexOf(it) ?: 1
                questionController.currentPosition = index
            }
        }
        binding.apply {
            appBarExam.apply {
                navigationToggleButton.setOnClickListener {
                    drawerLayout.open()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.exam, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_exam)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCountDown(seconds: Int) {
        if (!::binding.isInitialized) return
        updateCountLabel(seconds)
    }

    private var isDialogVisible = false
    override fun showErrorMessage(
        title: String,
        message: String,
        actionText: String,
        actionCallback: () -> Unit
    ) {
        if (isDialogVisible) return
        isDialogVisible = true
        DialogBottom.dismissAll(this)

        DialogBottom
            .Builder(this)
            .apply {
                this.title = title
                this.message = message
                acceptText = actionText
                acceptHandler = {
                    isDialogVisible = false
                    actionCallback()
                    true
                }
            }
            .show().apply {
                dismissible = false
            }
            .onDismissCallback {
                isDialogVisible = false
            }
    }

    override fun getExamId(): String {
        return examId
    }

    @SuppressLint("DefaultLocale")
    fun updateCountLabel(seconds: Int) {
        val textColor = if (seconds <= 120) {
            val redIntensity = (255 * (1 - (seconds / 120)))
            val color = Color.rgb(redIntensity, 0, 0)
            color
        } else { getAttr(this, android.R.attr.textColor) }
        runOnUiThread {
            binding.appBarExam.countdownLabel.apply {
                setTextColor(textColor)
                this.text = if(seconds > 60) {
                    val minute = seconds / 60.0
                    String.format("%.2fs", minute)
                } else {
                    String.format("%ds", seconds)
                }
            }
        }
    }

    companion object {
        const val EXAM_ID = "exam-id"
    }


}