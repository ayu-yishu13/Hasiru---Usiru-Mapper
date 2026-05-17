package com.hasiru.usiru.mapper.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.hasiru.usiru.mapper.data.local.TreeEntity
import kotlinx.coroutines.tasks.await

class FirestoreService(private val firestore: FirebaseFirestore) {
    private val collection = firestore.collection("community_trees")

    suspend fun uploadTree(tree: TreeEntity): String {
        val document = collection.add(tree.toFirestoreMap()).await()
        return document.id
    }

    suspend fun fetchTrees(): List<TreeEntity> = collection.get().await().documents.mapNotNull { doc ->
        val data = doc.data ?: return@mapNotNull null
        TreeEntity(
            latitude = (data["latitude"] as? Number)?.toDouble() ?: return@mapNotNull null,
            longitude = (data["longitude"] as? Number)?.toDouble() ?: return@mapNotNull null,
            species = data["species"] as? String ?: "Unknown",
            scientificName = data["scientific_name"] as? String ?: "",
            kannadaName = data["kannada_name"] as? String ?: "",
            healthStatus = data["health_status"] as? String ?: "Good",
            isNative = data["is_native"] as? Boolean ?: false,
            isEmptyPit = data["is_empty_pit"] as? Boolean ?: false,
            girthCm = (data["girth_cm"] as? Number)?.toFloat() ?: 0f,
            speciesFactor = (data["species_factor"] as? Number)?.toFloat() ?: 1f,
            oxygenScore = (data["oxygen_score"] as? Number)?.toFloat() ?: 0f,
            photoUri = data["photo_uri"] as? String,
            taggedBy = data["tagged_by"] as? String ?: "Community",
            timestampMs = (data["timestamp_ms"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            syncedToFirebase = true
        )
    }

    private fun TreeEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "latitude" to latitude,
        "longitude" to longitude,
        "species" to species,
        "scientific_name" to scientificName,
        "kannada_name" to kannadaName,
        "health_status" to healthStatus,
        "is_native" to isNative,
        "is_empty_pit" to isEmptyPit,
        "girth_cm" to girthCm,
        "species_factor" to speciesFactor,
        "oxygen_score" to oxygenScore,
        "photo_uri" to photoUri,
        "tagged_by" to taggedBy,
        "timestamp_ms" to timestampMs,
        "synced_to_firebase" to true
    )
}
