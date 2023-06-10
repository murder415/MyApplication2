package com.example.myapplication2

data class MessageRequest(
    val text: String,
    val num_samples: Int,
    val length: Int
)

data class MessageResponse(
    val choices: List<Choice>,
    val completion_time: Double
)

data class Choice(
    val text: String,
    val index: Int,
    val logprobs: Logprobs,
    val finish_reason: String
)

data class Logprobs(
    val tokens: List<String>,
    val token_logprobs: List<Double>,
    val top_logprobs: List<Double>
)
