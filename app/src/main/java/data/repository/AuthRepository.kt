package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.local.TokenStore
import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.LoginRequest
import com.example.goalhabitapp.data.remote.dto.RegisterRequest

class AuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
    suspend fun register(email: String, password: String, name: String) {
        val token = api.register(RegisterRequest(email, password, name)).token
        tokenStore.saveToken(token)
    }

    suspend fun login(email: String, password: String) {
        val token = api.login(LoginRequest(email, password)).token
        tokenStore.saveToken(token)
    }

    suspend fun isAuthorized(): Boolean =
        try { api.me(); true } catch (_: Exception) { false }

    suspend fun logout() { tokenStore.clear() }
}
