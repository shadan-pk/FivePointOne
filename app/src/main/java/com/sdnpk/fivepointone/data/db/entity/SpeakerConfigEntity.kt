package com.sdnpk.fivepointone.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sdnpk.fivepointone.config.Role

@Entity(tableName = "speaker_configs")
data class SpeakerConfigEntity(
    @PrimaryKey val id: String,  // Speaker unique ID, e.g. IP or MAC
    val ip: String,
    val assignedRole: Role?,     // Your enum or class for LEFT, RIGHT, etc.
    val roomName: String
)
