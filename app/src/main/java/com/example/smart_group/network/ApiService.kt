package com.example.smart_group.network

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("ping")
    fun ping(): Call<String>
}
