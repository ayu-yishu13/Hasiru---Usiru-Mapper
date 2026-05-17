package com.hasiru.usiru.mapper.ui.speciesguide

data class SpeciesItem(
    val commonName: String,
    val kannadaName: String,
    val scientificName: String,
    val speciesFactor: Float,
    val isNative: Boolean,
    val descriptionEn: String,
    val descriptionKn: String,
    val placeholderColor: String
)
