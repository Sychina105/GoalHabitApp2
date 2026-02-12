package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.GoalTemplateDto

class TemplatesRepository(private val api: ApiService) {
    suspend fun getTemplates(): List<GoalTemplateDto> = api.goalTemplates()
}
