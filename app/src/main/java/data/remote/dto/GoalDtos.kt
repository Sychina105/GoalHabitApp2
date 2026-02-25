package com.example.goalhabitapp.data.remote.dto

data class GoalCreateRequest(
    val title: String,
    val description: String?,
    val goalType: String,       // QUANT | STEPS | HABIT_AS_GOAL
    val targetValue: Int?,
    val unit: String?,
    val deadline: String?,      // "YYYY-MM-DD" или null
    val priority: Int,
    //val status: String          // ACTIVE | PAUSED | DONE | CANCELED
    val showInProfile: Boolean = false
)

data class GoalUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val goalType: String? = null,
    val targetValue: Int? = null,
    val unit: String? = null,
    val deadline: String? = null,
    val priority: Int? = null,
    val status: String? = null,
    val showInProfile: Boolean = false
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
    val status: String,
    val showInProfileDto: Boolean = false
)
data class GoalStatusRequest(val status: String)

