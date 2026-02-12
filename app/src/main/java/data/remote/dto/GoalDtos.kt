package com.example.goalhabitapp.data.remote.dto

data class GoalCreateRequest(
    val title: String,
    val description: String?,
    val goalType: String,       // QUANT | STEPS | HABIT_AS_GOAL
    val targetValue: Int?,
    val unit: String?,
    val deadline: String?,      // "YYYY-MM-DD" или null
    val priority: Int,
    val status: String          // ACTIVE | PAUSED | DONE | CANCELED
)

data class GoalDto(
    val id: Long,
    val title: String,
    val description: String?,
    val goalType: String,
    val targetValue: Int?,
    val unit: String?,
    val progressValue: Int,
    val deadline: String?,
    val priority: Int,
    val status: String
)
