package com.example.chatbot

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.cloud.dialogflow.v2.DetectIntentRequest
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.QueryParameters
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionsSettings
import com.google.cloud.dialogflow.v2.TextInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone
import java.util.UUID
import com.example.myapplication2.R

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val messageList: MutableList<Message> = ArrayList()
    private lateinit var sendButton: Button
    private lateinit var messageEditText: EditText
    private val projectId = "test3-sbqt"
    private val sessionMap: MutableMap<String, String> = HashMap()
    var messages: MutableList<String> = mutableListOf()
    var text1:String =""
    var text2:String =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerView)
        sendButton = findViewById(R.id.sendButton)
        messageEditText = findViewById(R.id.messageEditText)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val messageAdapter = MessageAdapter(messageList, "user_id") // "user_id"를 원하는 사용자 식별자로 변경하세요
        recyclerView.adapter = messageAdapter



        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(applicationContext))
        }

        val py = Python.getInstance()
        val pyObject = py.getModule("chat")
        pyObject.callAttr("set_openai_api_key","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")
        val pyResult = pyObject.callAttr("main")
        val messagesList = pyResult.asList().map { it.toString() }
        messages = messagesList.toMutableList()






        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            text1 = messageText
            messageList.add(Message(messageText, true))
            messageEditText.setText("")

            CoroutineScope(Dispatchers.Main).launch {
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(applicationContext))
                }

                val obj = withContext(Dispatchers.IO) {
                    val py = Python.getInstance()
                    val pyObject = py.getModule("chat")
                    val pyMessages = messages.toTypedArray()
                    val pyResult = pyObject.callAttr("get_input", messageText, pyMessages)
                    val messagesList = pyResult.asList().map { it.toString() }
                    messages = messagesList.toMutableList()
                    val answer = pyObject.callAttr("return_answer").toString()
                    answer
                }

                println(obj)
                println(obj.toString())
                // Handle the response
                // Extract the fulfillment text from the response
                // Add the response to the message list and notify the adapter of the change
                text2 = obj
                messageList.add(Message(obj, false))
                messageAdapter.notifyDataSetChanged() // 데이터 변경 알림

            }
        }
    }

}
