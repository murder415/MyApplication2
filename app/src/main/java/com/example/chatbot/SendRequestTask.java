package com.example.chatbot;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SendRequestTask implements Runnable {

    private static final String TAG = "SendRequestTask";
    private String dialogflowKey;
    private String messageText;
    private List<Message> messageList;
    private MessageAdapter mAdapter;

    public SendRequestTask(String dialogflowKey, String messageText, List<Message> messageList, MessageAdapter mAdapter) {
        this.dialogflowKey = dialogflowKey;
        this.messageText = messageText;
        this.messageList = messageList;
        this.mAdapter = mAdapter;
    }

    @Override
    public void run() {
        try {
            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + dialogflowKey);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            String jsonRequest = "{ \"query\": \"" + messageText + "\", \"lang\": \"en\", \"sessionId\": \"1234567890\" }";
            OutputStream os = conn.getOutputStream();
            os.write(jsonRequest.getBytes("UTF-8"));
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            conn.disconnect();

            // Handle the response on the main thread
            String response = sb.toString();
            messageList.add(new Message(response, false));
            mAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "Exception caught: ", e);
        }
    }
}
