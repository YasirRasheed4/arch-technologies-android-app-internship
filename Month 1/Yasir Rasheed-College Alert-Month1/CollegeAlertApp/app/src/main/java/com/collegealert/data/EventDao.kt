package com.collegealert.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startMillis ASC")
    fun observeAll(): Flow<List<CampusEvent>>

    @Query("SELECT * FROM events")
    suspend fun getAll(): List<CampusEvent>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CampusEvent): Long

    @Update
    suspend fun update(event: CampusEvent)

    @Delete
    suspend fun delete(event: CampusEvent)
}
