package com.example.dhvani.ml

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Maps model output indices to human-readable labels.
 */
class LabelMapper(private val context: Context) {

    private var labels: List<String> = emptyList()

    init {
        loadLabels()
    }

    private fun loadLabels() {
        try {
            val jsonString = context.assets.open("models/labels.json").bufferedReader().use { it.readText() }
            labels = Json.decodeFromString<List<String>>(jsonString)
        } catch (e: IOException) {
            labels = emptyList()
        }
    }

    fun getLabel(index: Int): String {
        return if (index in labels.indices) labels[index] else "Unknown"
    }

    fun getAllLabels(): List<String> = labels
}
