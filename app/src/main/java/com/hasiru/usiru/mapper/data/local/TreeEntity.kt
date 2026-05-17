package com.hasiru.usiru.mapper.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trees", indices = [Index(value = ["latitude", "longitude"], unique = true)])
data class TreeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val species: String,
    val scientificName: String,
    val kannadaName: String,
    val healthStatus: String,
    val isNative: Boolean,
    val isEmptyPit: Boolean,
    val girthCm: Float,
    val speciesFactor: Float,
    val oxygenScore: Float,
    val photoUri: String?,
    val taggedBy: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val syncedToFirebase: Boolean = false
)
