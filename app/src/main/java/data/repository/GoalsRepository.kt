package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.*

class GoalsRepository(private val api: ApiService) {
    suspend fun list(): List<GoalDto> = api.getGoals()
    suspend fun create(req: GoalCreateRequest): GoalDto = api.createGoal(req)
    suspend fun delete(id: Long) = api.deleteGoal(id)

    suspend fun setStatus(id: Long, status: String) =
        api.setGoalStatus(id, GoalStatusRequest(status))

    suspend fun update(id: Long, req: GoalUpdateRequest): GoalDto =
        api.updateGoal(id, req)

    suspend fun addProgress(id: Long, delta: Int): GoalDto =
        api.addGoalProgress(id, GoalProgressRequest(delta))

    // ✅ STEPS
    suspend fun steps(goalId: Long): List<GoalStepDto> = api.goalSteps(goalId)
    suspend fun addStep(goalId: Long, title: String): GoalStepDto =
        api.addGoalStep(goalId, StepCreateRequest(title))

    suspend fun toggleStep(stepId: Long, done: Boolean): GoalStepDto =
        api.updateStep(stepId, StepUpdateRequest(isDone = done))

    suspend fun renameStep(stepId: Long, title: String): GoalStepDto =
        api.updateStep(stepId, StepUpdateRequest(title = title))

    suspend fun deleteStep(stepId: Long) = api.deleteStep(stepId)
}