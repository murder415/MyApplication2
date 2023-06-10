package com.example.chatbot;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryParameters;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;





public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Message> messageList = new ArrayList<>();
    private Button sendButton;
    private EditText messageEditText;
    private SessionsClient sessionsClient;
    private String projectId = "YOUR_PROJECT_ID";
    private Map<String, String> sessionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.myapplication2.R.layout.activity_chat);

        recyclerView = findViewById(com.example.myapplication2.R.id.recyclerView);
        sendButton = findViewById(com.example.myapplication2.R.id.sendButton);
        messageEditText = findViewById(com.example.myapplication2.R.id.messageEditText);

        sessionsClient = createSessionsClient();

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MessageAdapter(messageList, "chatbot");
        recyclerView.setAdapter(mAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageEditText.getText().toString();

                messageList.add(new Message(messageText, true));
                mAdapter.notifyDataSetChanged();

                messageEditText.setText("");

                String sessionId = getSessionId();
                String roleName = getRoleName();

                new SendRequestTask().execute(projectId, sessionId, roleName, messageText);
            }
        });
    }

    private SessionsClient createSessionsClient() {
        try {
            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.build();
            return SessionsClient.create(sessionsSettings);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating sessions client", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String getSessionId() {
        // Check if there's already a session ID for the current role
        String roleName = getRoleName();
        if (sessionMap.containsKey(roleName)) {
            return sessionMap.get(roleName);
        }

        // If not, create a new session ID and store it for the current role
        String sessionId = UUID.randomUUID().toString();
        sessionMap.put(roleName, sessionId);
        return sessionId;
    }

    private String getRoleName() {
        // TODO: Implement logic to get the role name for the current user
        // For example, you could use Firebase Authentication to get the user's role
        // In this example, we'll just return a hardcoded role name
        return "user";
    }

    private class SendRequestTask extends AsyncTask<String, Void, DetectIntentResponse> {
        @Override
        protected DetectIntentResponse doInBackground(String... strings) {
            String projectId = strings[0];
            String sessionId = strings[1];
            String roleName = strings[2];
            String message = strings[3];

            // Build the session name using the project ID, session ID, and role name
            SessionName sessionName = SessionName.of(projectId, sessionId + "-" + roleName);

            // Build the query input
            QueryInput queryInput = QueryInput.newBuilder()
                    .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US"))
                    .build();

            TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
            String timeZoneId = timeZone.getID();

            QueryParameters queryParameters = QueryParameters.newBuilder()
                    .setTimeZone(timeZoneId)
                    .build();

            // Build the detect intent request
            DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder()
                    .setSession(sessionName.toString())
                    .setQueryInput(queryInput)
                    .setQueryParams(queryParameters)
                    .build();

            // Send the detect intent request and return the response
            try {
                SessionsClient sessionsClient = createSessionsClient();
                DetectIntentResponse response = sessionsClient.detectIntent(detectIntentRequest);
                sessionsClient.close();
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(DetectIntentResponse response) {
            if (response == null) {
                Toast.makeText(ChatActivity.this, "Error getting response", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract the fulfillment text from the response
            String fulfillmentText = response.getQueryResult().getFulfillmentText();

            // Add the response to the message list and notify the adapter of the change
            messageList.add(new Message(fulfillmentText, false));
            mAdapter.notifyDataSetChanged();
        }
    }
}

