package com.example.dhvani.data.repository

import android.content.Context
import com.example.dhvani.data.model.QuizQuestion
import com.example.dhvani.data.model.SignCategory
import com.example.dhvani.data.model.SignItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.example.dhvani.data.model.*
import com.example.dhvani.data.prefs.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.dhvani.data.SignData
import java.util.Locale

@Singleton
class SignRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: AppPreferences
) {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons = _lessons.asStateFlow()

    private val _allSigns: List<SignItem> by lazy {
        val signs = mutableListOf<SignItem>()
        
        // Get list of all video files in assets/dataset to match correctly
        val datasetFiles = try {
            context.assets.list("dataset")?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        // Add Alphabets
        SignData.signsByAlphabets.forEach { alpha ->
            signs.add(SignItem(
                id = "alpha_$alpha",
                label = alpha,
                assetPath = "starter_signs/a-z/${alpha.lowercase()}.jpg",
                category = SignCategory.ALPHABET
            ))
        }

        // Add Categories
        SignData.categoryMapping.forEach { (category: SignCategory, words: List<String>) ->
            words.forEach { word ->
                val fileName = word.lowercase(Locale.ROOT).replace(" ", "_").replace("/", "_")
                
                val path = if (category == SignCategory.NUMBER) {
                    "starter_signs/1-10/$word.jpg"
                } else {
                    // Match the word with files in dataset (case insensitive check but using actual filename)
                    val matchedFile = datasetFiles.find { 
                        it.equals("$word.mp4", ignoreCase = true) || 
                        it.startsWith("$word ", ignoreCase = true) ||
                        it.startsWith("$word[", ignoreCase = true) ||
                        it.startsWith("$word(", ignoreCase = true)
                    }
                    if (matchedFile != null) "dataset/$matchedFile" else "dataset/$word.mp4"
                }

                signs.add(SignItem(
                    id = "${category.name.lowercase(Locale.ROOT)}_$fileName",
                    label = word,
                    assetPath = path,
                    category = category
                ))
            }
        }
        signs
    }

    init {
        createLessons()
    }

    private fun createLessons() {
        val lessonList = mutableListOf<Lesson>()
        val completedIds = preferences.completedLessons
        var globalOrder = 0

        // Preferred order of categories for the curriculum
        val orderedCategories = listOf(
            SignCategory.ALPHABET,
            SignCategory.NUMBER,
            SignCategory.BASICS,
            SignCategory.FAMILY,
            SignCategory.EMOTIONS,
            SignCategory.BODY,
            SignCategory.COLORS,
            SignCategory.ANIMALS,
            SignCategory.HOME,
            SignCategory.FOOD,
            SignCategory.TIME,
            SignCategory.EDUCATION,
            SignCategory.TRANSPORT,
            SignCategory.ACTIONS,
            SignCategory.WORK,
            SignCategory.TECHNOLOGY,
            SignCategory.GRAMMAR,
            SignCategory.SPORTS,
            SignCategory.RELIGION,
            SignCategory.MISC
        )

        orderedCategories.forEach { category ->
            val signsInCategory = _allSigns.filter { it.category == category }
            if (signsInCategory.isEmpty()) return@forEach

            // Divide category into chunks of 5 for progressive lessons
            signsInCategory.chunked(5).forEachIndexed { index, signs ->
                val id = "lesson_${category.name.lowercase(Locale.ROOT)}_$index"
                val steps = generateStepsForSigns(signs)
                
                val isCompleted = completedIds.contains(id)
                val isPreviousCompleted = if (globalOrder == 0) true 
                    else completedIds.contains(lessonList.lastOrNull()?.id ?: "")

                val difficulty = when {
                    category == SignCategory.ALPHABET || category == SignCategory.NUMBER || category == SignCategory.BASICS -> "Beginner"
                    category == SignCategory.FAMILY || category == SignCategory.EMOTIONS || category == SignCategory.BODY || category == SignCategory.COLORS -> "Beginner"
                    category == SignCategory.ANIMALS || category == SignCategory.HOME || category == SignCategory.FOOD || category == SignCategory.TIME -> "Intermediate"
                    else -> "Advanced"
                }

                lessonList.add(
                    Lesson(
                        id = id,
                        title = "${category.name.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} - $difficulty Part ${index + 1}",
                        signs = signs,
                        steps = steps,
                        category = category,
                        order = globalOrder++,
                        isLocked = !isPreviousCompleted,
                        status = when {
                            isCompleted -> LessonStatus.COMPLETED
                            isPreviousCompleted -> LessonStatus.AVAILABLE
                            else -> LessonStatus.LOCKED
                        }
                    )
                )
            }
        }
        
        _lessons.value = lessonList
    }

    private fun generateStepsForSigns(signs: List<SignItem>): List<LessonStep> {
        val steps = mutableListOf<LessonStep>()

        // 1. Learning Phase
        signs.forEach { steps.add(LessonStep.Learn(it)) }

        // 2. Quiz Phase
        signs.forEach { sign ->
            // Pick distractors from same category only (letters with letters, numbers with numbers)
            val sameCategory = _allSigns.filter {
                it.category == sign.category && it != sign
            }
            val distractors = sameCategory.shuffled().take(3)
            val options = (distractors + sign).shuffled()
            steps.add(LessonStep.Quiz(sign, options))
        }

        // 3. Matching Phase
        if (signs.size >= 3) {
            steps.add(LessonStep.Match(signs.take(4).map { MatchPair(it, it.label) }))
        }

        // 4. Camera Practice (AI)
        signs.take(2).forEach { steps.add(LessonStep.Camera(it)) }

        return steps
    }

    fun getLessonById(lessonId: String): Lesson? {
        return _lessons.value.find { it.id == lessonId }
    }

    suspend fun completeLesson(lessonId: String) {
        val completed = preferences.completedLessons.toMutableSet()
        completed.add(lessonId)
        preferences.completedLessons = completed
        createLessons()
    }

    enum class QuizDifficulty { EASY, MEDIUM, HARD }

    fun generateQuiz(
        category: SignCategory,
        count: Int = 10,
        difficulty: QuizDifficulty = QuizDifficulty.MEDIUM
    ): List<QuizQuestion> {
        val categorySigns = _allSigns.filter { it.category == category }
        if (categorySigns.isEmpty()) return emptyList()

        return categorySigns.shuffled().take(count).map { correctAnswer ->
            val distractorPool = when (category) {
                SignCategory.ALPHABET -> _allSigns.filter { it.category == SignCategory.ALPHABET }
                SignCategory.NUMBER -> _allSigns.filter { it.category == SignCategory.NUMBER }
                else -> _allSigns.filter { it.category == category }
            }

            // Ensure distractors don't include the correct answer and are from the same "type"
            val distractors = (distractorPool - correctAnswer).shuffled()
            
            val optionCount = when (difficulty) {
                QuizDifficulty.EASY -> 2
                QuizDifficulty.MEDIUM -> 4
                QuizDifficulty.HARD -> 6
            }

            val options = (distractors.take(optionCount - 1) + correctAnswer).shuffled()
            
            QuizQuestion(
                correctAnswer = correctAnswer,
                options = options
            )
        }
    }

    fun getAllSigns(): List<SignItem> = _allSigns

    fun getSignsByCategory(category: SignCategory): List<SignItem> {
        return _allSigns.filter { it.category == category }
    }

    fun searchSigns(query: String): List<SignItem> {
        return _allSigns.filter { it.label.contains(query, ignoreCase = true) }
    }

    fun getRandomQuiz(count: Int = 10): List<QuizQuestion> {
        if (_allSigns.isEmpty()) return emptyList()
        return List(count) {
            val correctAnswer = _allSigns.random()
            
            // Filter options to be within the same category (e.g., only Alphabets for an Alphabet question)
            val sameCategorySigns = _allSigns.filter { it.category == correctAnswer.category }
            val options = (sameCategorySigns - correctAnswer).shuffled().take(3) + correctAnswer

            QuizQuestion(correctAnswer, options.shuffled())
        }
    }
}
