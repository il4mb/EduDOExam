package com.il4mb.edudoexam.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.il4mb.edudoexam.models.Answer

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers")
    fun getAll(): List<Answer>

    @Query("SELECT * FROM answers WHERE examId == :examId")
    fun loadAllByExamId(examId: String): List<Answer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg answers: Answer)

    @Query("DELETE FROM answers WHERE examId == :examId")
    fun deleteExamId(examId: String)

    @Delete
    fun delete(user: Answer)
}