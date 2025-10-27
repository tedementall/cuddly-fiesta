package com.example.thehub.ui.profile

import com.example.thehub.data.model.User

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: User) : ProfileUiState
    data class Error(val message: String, val isUnauthorized: Boolean = false) : ProfileUiState
}
