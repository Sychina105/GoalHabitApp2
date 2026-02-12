package com.example.goalhabitapp.data.remote.dto

data class RegisterRequest(val email: String, val password: String, val name: String)
data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val token: String)
data class MeResponse(val id: Long)
