package com.example.thehub.data.repository

import com.example.thehub.data.model.User
import com.example.thehub.data.remote.UserApi

class UserRepository(
    private val api: UserApi,
) {
    suspend fun getCurrentUser(): User = api.getCurrentUser()
}
