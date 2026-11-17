package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "codes")
data class CodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val format: String, // e.g. "QR_CODE", "CODE_128", "WIFI"
    val type: String, // "SCAN" or "CREATE"
    val timestamp: Long = System.currentTimeMillis(),
    val folderName: String? = null,
    val isFavorite: Boolean = false
)
