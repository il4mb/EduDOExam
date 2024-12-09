package com.capstone.edudoexam.api.response

import com.capstone.edudoexam.models.StudentExamResult
import com.capstone.edudoexam.models.TeacherExamResult

class ResponseStudentExamResult(
    error: Boolean,
    message: String,
    val result: StudentExamResult
) : Response(error, message)

class ResponseTeacherExamResult(
    error: Boolean,
    message: String,
    val result: TeacherExamResult
) : Response(error, message)