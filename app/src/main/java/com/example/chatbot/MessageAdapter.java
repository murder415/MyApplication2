package com.example.chatbot;
/*

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.example.myapplication2.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String userId;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public LinearLayout messageLinearLayout;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = v.findViewById(R.id.messageTextView);
            messageLinearLayout = v.findViewById(R.id.messageLinearLayout);
        }
    }

    public MessageAdapter(List<Message> messageList, String userId) {
        this.messageList = messageList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        MessageViewHolder vh = new MessageViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.messageTextView.setText(message.getMessageText());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        if (message.isSentByUser()) {
            params.setMargins(100, 10, 10, 10);
            holder.messageTextView.setBackgroundResource(R.drawable.user_message_background);
        } else {
            params.setMargins(10, 10, 100, 10);
            holder.messageTextView.setBackgroundResource(R.drawable.bot_message_background);
        }

        holder.messageLinearLayout.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}


 */