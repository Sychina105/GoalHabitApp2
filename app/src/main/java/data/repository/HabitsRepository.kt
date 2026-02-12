package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.*

class HabitsRepository(private val api: ApiService) {
    suspend fun list(): List<HabitDto> = api.getHabits()
    suspend fun create(req: HabitCreateRequest): HabitDto = api.createHabit(req)
    suspend fun checkIn(id: Long, date: String, value: Int? = null) =
        api.checkIn(id, CheckInRequest(date, value))
}
