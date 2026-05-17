package com.hasiru.usiru.mapper.ai

data class TreeIdentificationData(
    val species: String,
    val scientificName: String,
    val kannadaName: String,
    val health: String,
    val isNative: Boolean,
    val speciesFactor: Float,
    val description: String
)

sealed class TreeIdentificationResult {
    data class Success(val data: TreeIdentificationData) : TreeIdentificationResult()
    data class Error(val message: String) : TreeIdentificationResult()
}
