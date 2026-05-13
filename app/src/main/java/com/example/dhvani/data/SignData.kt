package com.example.dhvani.data

import com.example.dhvani.data.model.SignCategory

object SignData {

    // A-Z Alphabets
    val signsByAlphabets: List<String> = ('A'..'Z').map { it.toString() }

    // Category mapping based on all SignCategory enums defined in SignItem.kt
    val categoryMapping: Map<SignCategory, List<String>> = mapOf(

        SignCategory.NUMBER to listOf(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
        ),

        SignCategory.BASICS to listOf(
            "Hello", "Thank You", "Please", "Sorry", "Yes", "No",
            "Help", "Good", "Bad", "More"
        ),

        SignCategory.FAMILY to listOf(
            "Mother", "Father", "Sister", "Brother", "Baby",
            "Grandmother", "Grandfather", "Family", "Husband", "Wife"
        ),

        SignCategory.EMOTIONS to listOf(
            "Happy", "Sad", "Angry", "Scared", "Surprised",
            "Love", "Cry", "Laugh", "Tired", "Bored"
        ),

        SignCategory.BODY to listOf(
            "Hand", "Eye", "Ear", "Nose", "Mouth",
            "Head", "Leg", "Arm", "Heart", "Back"
        ),

        SignCategory.COLORS to listOf(
            "Red", "Blue", "Green", "Yellow", "Black",
            "White", "Orange", "Purple", "Pink", "Brown"
        ),

        SignCategory.ANIMALS to listOf(
            "Dog", "Cat", "Bird", "Fish", "Cow",
            "Horse", "Elephant", "Lion", "Tiger", "Rabbit"
        ),

        SignCategory.HOME to listOf(
            "House", "Door", "Window", "Kitchen", "Bed",
            "Chair", "Table", "Bathroom", "Garden", "Room"
        ),

        SignCategory.FOOD to listOf(
            "Water", "Food", "Milk", "Rice", "Bread",
            "Apple", "Banana", "Egg", "Sugar", "Salt"
        ),

        SignCategory.TIME to listOf(
            "Morning", "Evening", "Night", "Today", "Tomorrow",
            "Yesterday", "Week", "Month", "Year", "Now"
        ),

        SignCategory.EDUCATION to listOf(
            "School", "Book", "Teacher", "Student", "Learn",
            "Write", "Read", "Class", "Exam", "Study"
        ),

        SignCategory.TRANSPORT to listOf(
            "Car", "Bus", "Train", "Bicycle", "Boat",
            "Plane", "Walk", "Road", "Stop", "Drive"
        ),

        SignCategory.ACTIONS to listOf(
            "Eat", "Drink", "Sleep", "Run", "Jump",
            "Sit", "Stand", "Go", "Come", "Give"
        ),

        SignCategory.WORK to listOf(
            "Work", "Job", "Office", "Doctor", "Police",
            "Engineer", "Farmer", "Cook", "Driver", "Manager"
        ),

        SignCategory.TECHNOLOGY to listOf(
            "Phone", "Computer", "Internet", "Camera", "Television",
            "Radio", "Message", "Call", "App", "Battery"
        ),

        SignCategory.GRAMMAR to listOf(
            "I", "You", "He", "She", "We",
            "They", "What", "Where", "When", "Why"
        ),

        SignCategory.SPORTS to listOf(
            "Cricket", "Football", "Basketball", "Tennis", "Swimming",
            "Running", "Cycling", "Boxing", "Chess", "Volleyball"
        ),

        SignCategory.RELIGION to listOf(
            "God", "Prayer", "Temple", "Church", "Mosque",
            "Faith", "Bless", "Peace", "Holy", "Worship"
        ),

        SignCategory.MISC to listOf(
            "Money", "Market", "Hospital", "Police", "Fire",
            "Rain", "Sun", "Tree", "Flower", "Earth"
        )
    )
}

