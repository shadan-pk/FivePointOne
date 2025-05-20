package com.sdnpk.fivepointone.data.db.dao

import androidx.room.*
import com.sdnpk.fivepointone.data.db.entity.SpeakerConfigEntity

@Dao
interface SpeakerConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: SpeakerConfigEntity)

    @Query("SELECT * FROM speaker_configs")
    suspend fun getAll(): List<SpeakerConfigEntity>

    @Query("SELECT * FROM speaker_configs WHERE id = :id")
    suspend fun getById(id: String): SpeakerConfigEntity?

    @Delete
    suspend fun delete(config: SpeakerConfigEntity)
}
