package com.example.goalhabitapp.data.remote

import com.example.goalhabitapp.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): TokenResponse

    @GET("auth/me")
    suspend fun me(): MeResponse

    @GET("templates/goals")
    suspend fun goalTemplates(): List<GoalTemplateDto>
}
