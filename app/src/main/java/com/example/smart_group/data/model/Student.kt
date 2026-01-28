package com.example.smart_group.data.model

// NOTE: ברירות מחדל חשובות כדי ש-Firestore יוכל ליצור אובייקט בלי קונסטרקטור מלא
data class Student(
    val studentId: String = "",        // UUID
    val userId: String = "",           // firebase uid
    val userName: String = "",
    val email: String = "",

    //  answers לפי הסכמה
    val answers: Answers = Answers(),

    //  כרגע לא חייבים לנרמל - נשמור ריק ונוסיף אחרי זה
    val normalizedAnswers: List<Int> = emptyList(),

    //  סימון שהשאלון הושלם
    val questionnaireCompleted: Boolean = false
)

//  תואם  לסכמה שצריכה להיות:
// gender, genderPreference = single (String)
// availability, workStyle, workMode, language, taskPreference = multi (List<String>)
data class Answers(
    val gender: String = "",                   // "male" / "female"
    val genderPreference: String = "",         // "men"/"women"/"no_preference"

    val availability: List<String> = emptyList(),    // ["morning", ...]
    val workStyle: List<String> = emptyList(),       // ["individual"] / ["collaborative"]
    val workMode: List<String> = emptyList(),        // ["oncampus"] / ["remote"]
    val language: List<String> = emptyList(),        // ["Hebrew"] / ["English"] / ["Arabic"]
    val taskPreference: List<String> = emptyList()   // ["fixed"] / ["flexible"]
)
