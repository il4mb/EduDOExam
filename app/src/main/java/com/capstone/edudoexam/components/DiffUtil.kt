package com.capstone.edudoexam.components

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.capstone.edudoexam.models.Answer
import com.capstone.edudoexam.models.Exam
import com.capstone.edudoexam.models.ExamResult
import com.capstone.edudoexam.models.Question
import com.capstone.edudoexam.models.User

class ExamDiffCallback : DiffUtil.ItemCallback<Exam>() {
    override fun areItemsTheSame(oldItem: Exam, newItem: Exam): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Exam, newItem: Exam): Boolean {
        return oldItem == newItem
    }
}

class ExamResultDiffCallback : DiffUtil.ItemCallback<ExamResult>() {
    override fun areItemsTheSame(oldItem: ExamResult, newItem: ExamResult): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ExamResult, newItem: ExamResult): Boolean {
        return oldItem == newItem
    }
}

class AnswerDiffCallback : DiffUtil.ItemCallback<Answer>() {
    override fun areItemsTheSame(oldItem: Answer, newItem: Answer): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Answer, newItem: Answer): Boolean {
        return oldItem == newItem
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
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