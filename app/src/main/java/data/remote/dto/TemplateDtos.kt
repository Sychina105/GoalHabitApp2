package com.example.goalhabitapp.data.remote.dto

data class GoalTemplateDto(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val suggestedTarget: Int?,
    val suggestedUnit: String?
)
