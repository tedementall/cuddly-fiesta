package com.example.thehub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thehub.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ProfileViewModel(
    private val repository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading
            try {
                val user = repository.getCurrentUser()
                _state.value = ProfileUiState.Success(user)
            } catch (http: HttpException) {
                val isUnauthorized = http.code() == 401
                val message = if (isUnauthorized) {
                    "Sesión expirada"
                } else {
                    http.message()
                        .takeIf { !it.isNullOrBlank() }
                        ?: "Error al cargar el perfil"
                }
                _state.value = ProfileUiState.Error(message, isUnauthorized)
            } catch (io: IOException) {
                _state.value = ProfileUiState.Error("Error de red, revisa tu conexión")
            } catch (t: Throwable) {
                _state.value = ProfileUiState.Error(t.message ?: "Error desconocido")
            }
        }
    }

    class Factory(
        private val repository: UserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
            return ProfileViewModel(repository) as T
        }
    }
}
