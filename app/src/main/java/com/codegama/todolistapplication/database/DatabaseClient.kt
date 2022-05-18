package com.codegama.todolistapplication.database

import android.content.Context
import androidx.room.Room
import com.codegama.todolistapplication.database.AppDatabase

class DatabaseClient private constructor(private val mCtx: Context?) {
    //our app database object
    private val appDatabase: AppDatabase?
    fun getAppDatabase(): AppDatabase? {
        return appDatabase
    }

    companion object {
        private var mInstance: DatabaseClient? = null
        @Synchronized
        fun getInstance(mCtx: Context?): DatabaseClient? {
            if (mInstance == null) {
                mInstance = DatabaseClient(mCtx)
            }
            return mInstance
        }
    }

    init {
        appDatabase = mCtx?.let {
            Room.databaseBuilder(it, AppDatabase::class.java, "Task.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}