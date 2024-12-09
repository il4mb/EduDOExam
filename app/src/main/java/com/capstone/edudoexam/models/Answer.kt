package com.capstone.edudoexam.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity("answers")
data class Answer(
    @PrimaryKey val questionId: String,
    @ColumnInfo(name = "choice") var choice: Char?,
    @ColumnInfo(name = "summary") var summary: String,
    @ColumnInfo(name = "examId") var examId: String
) {

    var summaryMap: MutableMap<String, Int>
        get() {
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            return Gson().fromJson(this.summary, type)
        }
        set(value) {
            this.summary = Gson().toJson(value)
        }

    companion object {
        val MutableMap<String, Int>.toJson: String get() {
            return Gson().toJson(this)
        }
    }
}