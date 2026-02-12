package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.ProfileDto

class ProfileRepository(private val api: ApiService) {
    suspend fun load(): ProfileDto = api.getProfile()
}
