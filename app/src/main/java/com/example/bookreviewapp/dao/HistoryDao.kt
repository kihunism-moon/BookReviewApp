package com.example.bookreviewapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookreviewapp.model.History


@Dao
interface HistoryDao {

    @Query("select * from history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("delete from history where keyword == :keyword")
    fun delete(keyword: String)
}