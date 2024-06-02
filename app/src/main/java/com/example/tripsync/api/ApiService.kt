package com.example.tripsync.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val user: User)
data class User(val id: Int, val username: String, val email: String)
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val telemovel: String,
    val nome: String,
    val foto: ByteArray?
)
data class RegisterResponse(
    val success: Boolean,
    val message: String
)

interface ApiService {
    @POST("api/v2/auth/signin")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/v2/auth/signup")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}
