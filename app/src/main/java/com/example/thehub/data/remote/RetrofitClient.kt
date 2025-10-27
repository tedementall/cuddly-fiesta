package com.example.thehub.data.remote

import com.example.thehub.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private fun baseClient(vararg extras: okhttp3.Interceptor): OkHttpClient {
        val log = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(log)
            .apply { extras.forEach { addInterceptor(it) } }
            // timeouts útiles para upload de imágenes
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun auth(): XanoAuthApi =
        Retrofit.Builder()
            .baseUrl(ensureSlash(BuildConfig.XANO_AUTH_BASE))
            .addConverterFactory(GsonConverterFactory.create())
            .client(baseClient())
            .build()
            .create(XanoAuthApi::class.java)

    fun store(vararg interceptors: okhttp3.Interceptor): XanoMainApi =
        Retrofit.Builder()
            .baseUrl(ensureSlash(BuildConfig.XANO_STORE_BASE))
            .addConverterFactory(GsonConverterFactory.create())
            .client(baseClient(*interceptors))
            .build()
            .create(XanoMainApi::class.java)

    fun user(vararg interceptors: okhttp3.Interceptor): UserApi =
        Retrofit.Builder()
            .baseUrl(ensureSlash(BuildConfig.XANO_AUTH_BASE))
            .addConverterFactory(GsonConverterFactory.create())
            .client(baseClient(*interceptors))
            .build()
            .create(UserApi::class.java)

    fun upload(vararg interceptors: okhttp3.Interceptor): UploadService =
        Retrofit.Builder()
            .baseUrl(ensureSlash(BuildConfig.XANO_STORE_BASE))
            .addConverterFactory(GsonConverterFactory.create())
            .client(baseClient(*interceptors))
            .build()
            .create(UploadService::class.java)


    private fun ensureSlash(base: String): String =
        if (base.endsWith("/")) base else "$base/"
}
