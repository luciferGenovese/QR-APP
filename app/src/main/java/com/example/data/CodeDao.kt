package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeDao {
    @Query("SELECT * FROM codes ORDER BY timestamp DESC")
    fun getAllCodes(): Flow<List<CodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: CodeEntity): Long

    @androidx.room.Update
    suspend fun updateCode(code: CodeEntity)

    @Query("DELETE FROM codes WHERE id = :id")
    suspend fun deleteCodeById(id: Int)

    @Query("DELETE FROM codes")
    suspend fun deleteAllCodes()
}
