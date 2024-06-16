package com.example.tripsync.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.ArrayList

//Auth Data
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val code: Int, val id: String, val nome: String, val token: String)
data class RegisterRequest(
    val username: String,
    val nome: String,
    val email: String,
    val telemovel: Int,
    val password: String,
    val foto: String?
)
data class RegisterResponse(
    val code: Int,
    val message: String
)

//User Data
data class User(
    val id_utilizador: String,
    val nome: String,
    val username: String,
    val telemovel: Int,
    val email: String,
    val password: String,
    val foto: Any? = null
)

//Trips Data
data class Trip(
    val titulo: String,
    val descricao: String,
    val cidade: String,
    val pais: String,
    val data_inicio: String,
    val data_fim: String,
    val custos: Float,
    val classificacao: Int,
    val foto: Any? = null,
    val id_utilizador: String
    )

//Requests
interface ApiService {
    @POST("api/v2/auth/signin")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/v2/auth/signup")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @GET("api/v2/users/{id}")
    fun getUserDetails(@Path("id") userId: String): Call<User>

    @PUT("api/v2/users/update/{id}")
    fun updateUserDetails(@Path("id") userId: String, @Body user: User): Call<User>

    @GET("api/v2/viagens/{id}")
    fun getTripDetails(@Path("id") viagemId: String): Call<Trip>

    @GET("api/v2/viagens/byUser/{id}")
    fun getViagensByUser(@Path("id") userId: String): Call<List<Trip>>

    @POST("api/v2/viagens/create")
    fun adicionarViagem(@Body trip: Trip): Call<Void>

    @PUT("api/v2/viagens/update/{id}")
    fun updateTripDetails(@Path("id") tripId: String, @Body trip: Trip): Call<Trip>

}
