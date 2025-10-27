package com.example.thehub.di

import android.content.Context
import com.example.thehub.data.remote.AuthInterceptor
import com.example.thehub.data.remote.RetrofitClient
import com.example.thehub.data.remote.UploadService
import com.example.thehub.data.remote.UserApi
import com.example.thehub.data.remote.XanoAuthApi
import com.example.thehub.data.remote.XanoMainApi
import com.example.thehub.data.repository.AuthRepository
import com.example.thehub.data.repository.ProductRepository
import com.example.thehub.data.repository.UserRepository
import com.example.thehub.utils.TokenStore

object ServiceLocator {

    private lateinit var appContext: Context


    fun init(context: Context) {
        appContext = context.applicationContext
        TokenStore.init(appContext)
    }


    private val authInterceptor: AuthInterceptor by lazy { AuthInterceptor { TokenStore.get() } }

    // APIs
    private val authApi: XanoAuthApi by lazy { RetrofitClient.auth() }
    private val storeApi: XanoMainApi by lazy { RetrofitClient.store(authInterceptor) }
    private val userApi: UserApi by lazy { RetrofitClient.user(authInterceptor) }
    val uploadService: UploadService by lazy { RetrofitClient.upload(authInterceptor) }

    // Repos
    val authRepository: AuthRepository by lazy { AuthRepository(authApi) }
    val productRepository: ProductRepository by lazy { ProductRepository(storeApi) }
    val userRepository: UserRepository by lazy { UserRepository(userApi) }
}
