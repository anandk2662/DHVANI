package com.example.dhvani.data.repository

import android.content.Context
import com.example.dhvani.data.model.QuizQuestion
import com.example.dhvani.data.model.SignCategory
import com.example.dhvani.data.model.SignItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

import com.example.dhvani.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SignRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons = _lessons.asStateFlow()

    private val alphabets = ('A'..'Z').map { char ->
        SignItem(
            id = "alpha_$char",
            label = char.toString(),
            assetPath = "starter_signs/a-z/${char.lowercase()}.jpg",
            category = SignCategory.ALPHABET
        )
    }

    private val numbers = (0..9).map { num ->
        SignItem(
            id = "num_$num",
            label = num.toString(),
            assetPath = "starter_signs/1-10/$num.jpg",
            category = SignCategory.NUMBER
        )
    }

    init {
        createLessons()
    }

    private fun createLessons() {
        val lessonList = mutableListOf<Lesson>()
        val allSigns = alphabets + numbers
        
        // Alphabets Lessons (4 per lesson)
        alphabets.chunked(4).forEachIndexed { index, signs ->
            val steps = mutableListOf<LessonStep>()
            
            // Step 1: Learn each sign
            signs.forEach { steps.add(LessonStep.Learn(it)) }
            
            // Step 2: Quiz for each sign
            signs.forEach { sign ->
                val options = (allSigns - sign).shuffled().take(3) + sign
                steps.add(LessonStep.Quiz(sign, options.shuffled()))
            }
            
            // Step 3: Match activity
            steps.add(LessonStep.Match(signs.map { MatchPair(it, it.label) }))
            
            // Step 4: Fill in the Blank (if we have at least 2 signs)
            if (signs.size >= 2) {
                steps.add(LessonStep.FillBlank(
                    sentence = "The first letter of the alphabet is [BLANK]",
                    answerSign = signs[0],
                    options = signs.shuffled()
                ))
            }

            // Step 5: Rearrange
            steps.add(LessonStep.Rearrange(
                targetSentence = signs.joinToString("") { it.label },
                scrambledWords = signs.map { it.label }.shuffled(),
                wordSigns = signs.associateBy { it.label }
            ))
            
            // Step 6: Camera Practice for each sign
            signs.forEach { steps.add(LessonStep.Camera(it)) }

            // Step 7: Timed Challenge
            steps.add(LessonStep.TimedChallenge(signs.shuffled(), timeLimitSeconds = 20))

            lessonList.add(
                Lesson(
                    id = "lesson_alpha_$index",
                    title = "Alpha ${signs.first().label}-${signs.last().label}",
                    signs = signs,
                    steps = steps,
                    category = SignCategory.ALPHABET,
                    order = index,
                    isLocked = index > 0,
                    status = if (index == 0) LessonStatus.AVAILABLE else LessonStatus.LOCKED
                )
            )
        }

        // Numbers Lessons
        numbers.chunked(4).forEachIndexed { index, signs ->
            val order = alphabets.size / 4 + index
            val steps = mutableListOf<LessonStep>()
            
            signs.forEach { steps.add(LessonStep.Learn(it)) }
            signs.forEach { sign ->
                val options = (allSigns - sign).shuffled().take(3) + sign
                steps.add(LessonStep.Quiz(sign, options.shuffled()))
            }
            steps.add(LessonStep.Match(signs.map { MatchPair(it, it.label) }))
            steps.add(LessonStep.TimedChallenge(signs.shuffled(), timeLimitSeconds = 15))

            lessonList.add(
                Lesson(
                    id = "lesson_num_$index",
                    title = "Numbers ${signs.first().label}-${signs.last().label}",
                    signs = signs,
                    steps = steps,
                    category = SignCategory.NUMBER,
                    order = order,
                    isLocked = true,
                    status = LessonStatus.LOCKED
                )
            )
        }
        
        _lessons.value = lessonList
    }

    fun getLessonById(lessonId: String): Lesson? {
        return _lessons.value.find { it.id == lessonId }
    }

    suspend fun completeLesson(lessonId: String) {
        val currentLessons = _lessons.value.toMutableList()
        val index = currentLessons.indexOfFirst { it.id == lessonId }
        if (index != -1) {
            val completedLesson = currentLessons[index].copy(status = LessonStatus.COMPLETED)
            currentLessons[index] = completedLesson
            
            // Unlock next lesson
            if (index + 1 < currentLessons.size) {
                val nextLesson = currentLessons[index + 1].copy(
                    isLocked = false,
                    status = LessonStatus.AVAILABLE
                )
                currentLessons[index + 1] = nextLesson
            }
            _lessons.value = currentLessons
        }
    }

    fun getAlphabets(): List<SignItem> = alphabets
    fun getNumbers(): List<SignItem> = numbers
    fun getAllSigns(): List<SignItem> = alphabets + numbers

    fun getRandomQuiz(count: Int = 10): List<QuizQuestion> {
        val allSigns = getAllSigns()
        if (allSigns.isEmpty()) return emptyList()

        return List(count) {
            val correctAnswer = allSigns.random()
            val options = (allSigns - correctAnswer).shuffled().take(3) + correctAnswer
            QuizQuestion(correctAnswer, options.shuffled())
        }
    }
}
