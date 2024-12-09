package com.capstone.edudoexam.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.capstone.edudoexam.models.Answer

@Database(entities = [Answer::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun answerDao(): AnswerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edudoexam-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
