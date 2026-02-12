package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.*

class FriendsRepository(private val api: ApiService) {
    suspend fun search(q: String?) = api.users(q)
    suspend fun add(id: Int) = api.addFriend(id)
    suspend fun friends() = api.friends()
    suspend fun profile(id: Int) = api.friendProfile(id)
}

