package com.example.dhvani.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SignItem(
    val id: String,
    val label: String,
    val assetPath: String,
    val category: SignCategory
)

enum class SignCategory {
    ALPHABET,
    NUMBER,
    BASICS,
    FAMILY,
    EMOTIONS,
    BODY,
    HOME,
    FOOD,
    ANIMALS,
    TIME,
    EDUCATION,
    TECHNOLOGY,
    TRANSPORT,
    WORK,
    ACTIONS,
    COLORS,
    GRAMMAR,
    RELIGION,
    SPORTS,
    MISC
}

data class QuizQuestion(
    val correctAnswer: SignItem,
    val options: List<SignItem>
)

data class DictionaryItem(
    val sign: SignItem,
    val description: String = ""
)
