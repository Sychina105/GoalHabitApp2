package com.example.goalhabitapp.data.repository

import com.example.goalhabitapp.data.remote.ApiService
import com.example.goalhabitapp.data.remote.dto.*

class FriendsRepository(private val api: ApiService) {

    suspend fun search(q: String?) = api.users(q)

    // отправить запрос / принять входящий (взаимка)
    suspend fun add(id: Int) = api.addFriend(id)

    // ✅ отменить исходящий / удалить из друзей
    suspend fun remove(id: Int) = api.removeFriend(id)

    // список взаимных друзей
    suspend fun friends() = api.friends()

    // профиль друга (доступ только при взаимке)
    suspend fun profile(id: Int) = api.friendProfile(id)
}
