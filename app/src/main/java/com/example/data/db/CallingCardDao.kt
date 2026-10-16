package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallingCardDao {
    @Query("SELECT * FROM calling_cards WHERE cardId = :cardId LIMIT 1")
    fun getCallingCardFlow(cardId: String): Flow<CallingCardEntity?>

    @Query("SELECT * FROM calling_cards WHERE cardId = :cardId LIMIT 1")
    suspend fun getCallingCard(cardId: String): CallingCardEntity?

    @Query("SELECT * FROM calling_cards ORDER BY updatedAt DESC")
    fun getAllCallingCards(): Flow<List<CallingCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(card: CallingCardEntity)

    @Query("DELETE FROM calling_cards WHERE cardId = :cardId")
    suspend fun delete(cardId: String)

    @Query("DELETE FROM calling_cards")
    suspend fun deleteAll()
}
