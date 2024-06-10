package com.example.tripsync.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.Date

data class User(
    val id_utilizador: String,
    val nome: String,
    val username: String,
    val telemovel: Int,
    val email: String,
    val password: String,
    val foto: ByteArray?
)
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val code: Int, val id: String, val nome: String, val token: String)
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val telemovel: Int,
    val nome: String,
    val foto: ByteArray?
)
data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class Trip(
    val titulo: String,
    val descricao: String,
    val cidade: String,
    val pais: String,
    val data_inicio: Date,
    val data_fim: Date,
    val custos: Float,
    val classificacao: String
    )
interface ApiService {
    @POST("api/v2/auth/signin")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/v2/auth/signup")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @GET("api/v2/users/{id}")
    fun getUserDetails(@Path("id") userId: String): Call<User>

    @PUT("api/v2/users/{id}")
    fun updateUserDetails(@Path("id") userId: String, @Body user: User): Call<User>

    @GET("api/v2/viagens/{id}")
    fun getTripDetails(@Path("id") viagemId: String): Call<Trip>
}
