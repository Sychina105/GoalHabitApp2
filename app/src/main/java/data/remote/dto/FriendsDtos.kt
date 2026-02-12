package com.example.goalhabitapp.data.remote.dto

data class PublicUserDto(
    val id: Int,
    val name: String,
    val email: String? = null
)

data class FriendProfileDto(
    val user: FriendUserDto,
    val currentHabitStreak: Int,
    val goalsCompleted: Int,
    val achievements: List<AchievementDto>
)

data class FriendUserDto(
    val id: Int,
    val name: String
)
