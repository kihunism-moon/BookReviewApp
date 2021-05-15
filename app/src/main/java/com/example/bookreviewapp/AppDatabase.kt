package com.example.bookreviewapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookreviewapp.dao.HistoryDao
import com.example.bookreviewapp.dao.ReviewDao
import com.example.bookreviewapp.model.History
import com.example.bookreviewapp.model.Review


@Database(entities = [History::class, Review::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao

}