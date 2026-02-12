package com.example.goalhabitapp.data.remote.dto

data class ProfileDto(
    val currentHabitStreak: Int,
    val goalsCompleted: Int,
    val achievements: List<AchievementDto>
)

data class AchievementDto(
    val code: String,
    val title: String,
    val earned_at: String?
)
