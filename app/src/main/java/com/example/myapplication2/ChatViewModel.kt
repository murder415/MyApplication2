package com.example.myapplication2
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication2.MessageRequest
import com.example.myapplication2.MessageResponse

class ChatViewModel : ViewModel() {

    val messageResponse: MutableLiveData<MessageResponse> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    private var apiKey: String = ""

    fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    fun sendMessage(request: MessageRequest) {
        // TODO: Implement sending the message and handling the response

        // 예시로 고정된 응답을 보내는 코드
        val response = MessageResponse(
            listOf(
                Choice("Hello!", 0, Logprobs(listOf("Hello!"), listOf(0.0), listOf(0.0)), "complete")
            ),
            1.5
        )
        messageResponse.value = response
    }
}
