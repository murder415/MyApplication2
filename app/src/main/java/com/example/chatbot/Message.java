package com.example.chatbot;

public class Message {
    private String messageText;
    private boolean sentByUser;

    public Message(String messageText, boolean sentByUser) {
        this.messageText = messageText;
        this.sentByUser = sentByUser;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }

    public void setSentByUser(boolean sentByUser) {
        this.sentByUser = sentByUser;
    }


}
