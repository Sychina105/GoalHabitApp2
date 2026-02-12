package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.*

class FriendsRepository(private val api: ApiService) {
    suspend fun searchUsers(q: String?): List<PublicUserDto> = api.users(q)
    suspend fun listFriends(): List<PublicUserDto> = api.friends()
    suspend fun add(id: Int) = api.addFriend(id)
    suspend fun remove(id: Int) = api.removeFriend(id)
    suspend fun profile(id: Int): FriendProfileDto = api.friendProfile(id)
}
