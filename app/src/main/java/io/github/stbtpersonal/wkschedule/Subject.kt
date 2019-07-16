package io.github.stbtpersonal.wkschedule

data class Subject(
    val type: String,
    val isUnlocked: Boolean,
    val character: String?,
    val characterImageUrls: String?,
    val meanings: List<String>,
    val readings: List<String>
)