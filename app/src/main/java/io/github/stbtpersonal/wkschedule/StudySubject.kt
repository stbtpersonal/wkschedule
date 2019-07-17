package io.github.stbtpersonal.wkschedule

data class StudySubject(
    val type: String,
    val isUnlocked: Boolean,
    val character: String?,
    val characterImageUrl: String?,
    val meanings: List<String>,
    val readings: List<String>
)