package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.*

class GoalsRepository(private val api: ApiService) {
    suspend fun list(): List<GoalDto> = api.getGoals()
    suspend fun create(req: GoalCreateRequest): GoalDto = api.createGoal(req)
}
