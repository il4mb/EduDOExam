package com.il4mb.edudoexam.components

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.il4mb.edudoexam.models.Exam
import com.il4mb.edudoexam.models.Participant
import com.il4mb.edudoexam.models.StudentExamResult
import com.il4mb.edudoexam.models.Question
import com.il4mb.edudoexam.models.User

class ExamDiffCallback : DiffUtil.ItemCallback<Exam>() {
    override fun areItemsTheSame(oldItem: Exam, newItem: Exam): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Exam, newItem: Exam): Boolean {
        return oldItem == newItem
    }
}

class ExamResultDiffCallback : DiffUtil.ItemCallback<StudentExamResult>() {
    override fun areItemsTheSame(oldItem: StudentExamResult, newItem: StudentExamResult): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: StudentExamResult, newItem: StudentExamResult): Boolean {
        return oldItem == newItem
    }
}

//
//class AnswerDiffCallback : DiffUtil.ItemCallback<Answer>() {
//    override fun areItemsTheSame(oldItem: Answer, newItem: Answer): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(oldItem: Answer, newItem: Answer): Boolean {
//        return oldItem == newItem
//    }
//}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}


class ParticipantDiffCallback : DiffUtil.ItemCallback<Participant>() {
    override fun areItemsTheSame(oldItem: Participant, newItem: Participant): Boolean {
        return oldItem.user.id == newItem.user.id
    }

    override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean {
        return oldItem == newItem
    }
}

class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
    override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
        return oldItem.id == newItem.id // Assuming id is unique
    }

    override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
        return oldItem.order == newItem.order && oldItem.description == newItem.description
        // Ensure to compare the 'order' field to trigger a rebind when the order changes
    }
}