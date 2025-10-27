package com.example.thehub.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
)
