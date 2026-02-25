package com.example.goalhabitapp.data.remote

import com.example.goalhabitapp.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.*

interface ApiService {

    // -------- AUTH --------
    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): TokenResponse

    @GET("auth/me")
    suspend fun me(): MeResponse

    // -------- TEMPLATES --------
    @GET("templates/goals")
    suspend fun goalTemplates(): List<GoalTemplateDto>

    // -------- HABITS --------
    @GET("habits")
    suspend fun getHabits(): List<HabitDto>

    @POST("habits")
    suspend fun createHabit(@Body req: HabitCreateRequest): HabitDto

    @POST("habits/{id}/checkin")
    suspend fun checkIn(
        @Path("id") id: Long,
        @Body req: CheckInRequest
    )

    // ✅ -------- GOALS --------
    @GET("goals")
    suspend fun getGoals(): List<GoalDto>

    @POST("goals")
    suspend fun createGoal(@Body req: GoalCreateRequest): GoalDto

    @GET("profile")
    suspend fun getProfile(): ProfileDto

    @GET("users")
    suspend fun users(@Query("q") q: String? = null): List<PublicUserDto>

    @GET("friends")
    suspend fun friends(): List<PublicUserDto>

    @POST("friends/{id}")
    suspend fun addFriend(@Path("id") id: Int)

    @DELETE("friends/{id}")
    suspend fun removeFriend(@Path("id") id: Int)

    @GET("profile/{id}")
    suspend fun friendProfile(@Path("id") id: Int): FriendProfileDto

    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") id: Long)

    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") id: Long)

    @PUT("goals/{id}/status")
    suspend fun setGoalStatus(
        @Path("id") id: Long,
        @Body req: GoalStatusRequest
    )

    @PUT("goals/{id}")
    suspend fun updateGoal(
        @Path("id") id: Long,
        @Body req: GoalUpdateRequest
    ): GoalDto

    @PUT("habits/{id}")
    suspend fun updateHabit(
        @Path("id") id: Long,
        @Body req: HabitUpdateRequest
    ): HabitDto


    @PUT("goals/{id}/progress")
    suspend fun addGoalProgress(
        @Path("id") id: Long,
        @Body req: GoalProgressRequest
    ): GoalDto

}
