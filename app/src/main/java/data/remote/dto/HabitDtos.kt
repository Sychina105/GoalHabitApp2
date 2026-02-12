package com.example.goalhabitapp.data.remote.dto

data class HabitCreateRequest(
    val title: String,
    val periodDays: Int,
    val timesPerPeriod: Int
)

data class HabitDto(
    val id: Long,
    val title: String,
    val periodDays: Int,
    val timesPerPeriod: Int
)

data class CheckInRequest(
    val date: String,
    val value: Int? = null
)
