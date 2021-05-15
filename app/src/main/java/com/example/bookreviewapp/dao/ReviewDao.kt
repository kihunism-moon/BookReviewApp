package com.example.bookreviewapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookreviewapp.model.Review

@Dao
interface ReviewDao {
    @Query("select * from review where id == :id")
    fun getOneReview(id: Int): Review

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReview(review: Review)
}