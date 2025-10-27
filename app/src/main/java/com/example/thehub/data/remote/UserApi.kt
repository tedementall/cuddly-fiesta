package com.example.thehub.data.remote

import com.example.thehub.data.model.User
import retrofit2.http.GET

interface UserApi {
    @GET("auth/me")
    suspend fun getCurrentUser(): User
}
