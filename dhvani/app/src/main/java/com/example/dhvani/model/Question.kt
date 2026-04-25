package com.example.dhvani.model

enum class QuestionType{
    MCQ,
    FILL_BLANK,
    MATCH,
    TRUE_FALSE,
    LISTEN_AND_SELECT,
    IMAGE_BASED
}

data class QuestionEntity(
    val id:Int,
    val quizId:Int,
    val lessonId:Int,
    val type: QuestionType,

    val question: String,
    val options: List<String>? = null,  // MCQ
    val answer:String?=null,  //FILL_BLANK / MCQ

    val matchPairs:List<Pair<String, String>>?=null, //Match the following

    val imageUrl:String? = null, //Image based
    val audioUrl: String ?=null  //Listening


)