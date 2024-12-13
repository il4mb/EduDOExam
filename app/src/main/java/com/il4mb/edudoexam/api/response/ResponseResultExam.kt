package com.il4mb.edudoexam.api.response

import com.il4mb.edudoexam.models.StudentExamResult
import com.il4mb.edudoexam.models.TeacherExamResult

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