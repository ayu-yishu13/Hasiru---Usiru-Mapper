package com.hasiru.usiru.mapper.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiTreeIdentifier(apiKey: String) {
    private val model = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)

    suspend fun identifyTree(bitmap: Bitmap): TreeIdentificationResult = withContext(Dispatchers.IO) {
        if (bitmap.width == 0 || bitmap.height == 0) return@withContext TreeIdentificationResult.Error("Invalid image")
        runCatching {
            val response = model.generateContent(content {
                image(bitmap)
                text(PROMPT)
            })
            parseResponse(response.text.orEmpty())
        }.getOrElse { TreeIdentificationResult.Error(it.localizedMessage ?: "Could not identify tree") }
    }

    private fun parseResponse(rawText: String): TreeIdentificationResult {
        val cleaned = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val json = JSONObject(cleaned)
        val error = json.optString("error")
        if (error.isNotBlank()) return TreeIdentificationResult.Error(error)
        return TreeIdentificationResult.Success(
            TreeIdentificationData(
                species = json.optString("species"),
                scientificName = json.optString("scientificName"),
                kannadaName = json.optString("kannadaName"),
                health = json.optString("health", "Good"),
                isNative = json.optBoolean("isNative", false),
                speciesFactor = json.optDouble("speciesFactor", 1.0).toFloat(),
                description = json.optString("description")
            )
        )
    }

    private companion object {
        private const val PROMPT = """You are a botanist expert in South Indian urban trees. Identify the tree in this photo.
Reply ONLY with valid JSON, no markdown, no explanation:
{
"species": "Common name in English",
"scientificName": "Latin binomial",
"kannadaName": "ಕನ್ನಡ ಹೆಸರು",
"health": "Good" or "Sick" or "Dead",
"isNative": true or false,
"speciesFactor": numeric (Neem=1.4, Peepal=1.6, Honge=1.2, Banyan=1.5, all others=1.0),
"description": "One sentence about this tree in Kannada and English"
}
If the photo does not contain a tree, return: {"error": "No tree detected"}"""
    }
}
