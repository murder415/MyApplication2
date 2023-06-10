package com.example.myapplication2


import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface ChatGPTService {
    @Headers("Content-Type: application/json")
    @POST("api/chat")
    suspend fun getMessage(@Body request: MessageRequest): MessageResponse
}

