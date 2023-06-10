package com.example.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.MessageAdapter.MessageViewHolder
import com.example.myapplication2.R

class MessageAdapter(private val messageList: List<Message>, private val userId: String) :
    RecyclerView.Adapter<MessageViewHolder>() {
    class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var messageTextView: TextView
        var messageLinearLayout: LinearLayout

        init {
            messageTextView = v.findViewById(R.id.messageTextView)
            messageLinearLayout = v.findViewById(R.id.messageLinearLayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(v)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.messageTextView.text = message.messageText
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        if (message.isSentByUser) {
            params.setMargins(100, 10, 10, 10)
            holder.messageTextView.setBackgroundResource(R.drawable.user_message_background)
        } else {
            params.setMargins(10, 10, 100, 10)
            holder.messageTextView.setBackgroundResource(R.drawable.bot_message_background)
        }
        holder.messageLinearLayout.layoutParams = params
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}