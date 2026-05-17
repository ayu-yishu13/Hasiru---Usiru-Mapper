package com.hasiru.usiru.mapper.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tree: TreeEntity): Long

    @Query("SELECT * FROM trees ORDER BY timestampMs DESC")
    fun getAllTrees(): Flow<List<TreeEntity>>

    @Query("SELECT * FROM trees WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    suspend fun getTreesInBounds(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<TreeEntity>

    @Query("SELECT COALESCE(SUM(oxygenScore), 0) FROM trees")
    fun getTotalOxygenScore(): Flow<Float>

    @Query("SELECT * FROM trees WHERE syncedToFirebase = 0")
    suspend fun getUnsynced(): List<TreeEntity>

    @Query("UPDATE trees SET syncedToFirebase = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)

    @Query("SELECT * FROM trees WHERE ABS(latitude - :lat) < 0.000001 AND ABS(longitude - :lng) < 0.000001 LIMIT 1")
    suspend fun findByLocation(lat: Double, lng: Double): TreeEntity?

    @Query("DELETE FROM trees")
    suspend fun deleteAll()
}
