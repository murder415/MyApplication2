/*
package com.example.myapplication2


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.MessageAdapter
import com.example.myapplication2.databinding.FragmentChatGptBinding

public class ChatGPTFragment extends Fragment implements TextToSpeech.OnInitListener {
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private EditText chatEditText;
    private TextToSpeech textToSpeech;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_gpt, container, false);

        chatRecyclerView = rootView.findViewById(R.id.chat_recycler_view);
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        chatEditText = rootView.findViewById(R.id.chat_edit_text);
        chatEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
        if (i == EditorInfo.IME_ACTION_SEND) {
            String chatMessage = textView.getText().toString();
            addChatMessage(chatMessage, true);
            generateChatResponse(chatMessage);
            textView.setText("");
            return true;
        }
        return false;
    });

        textToSpeech = new TextToSpeech(getActivity(), this);

        return rootView;
    }

    private void addChatMessage(String message, boolean isUser) {
        Chat chat = new Chat(message, isUser);
        chatList.add(chat);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
        if (!isUser) {
            speakText(message);
        }
    }

    private void generateChatResponse(String userMessage) {
        // Generate chat response using ChatGPT API
        String chatbotMessage = generateChatbotMessage(userMessage);
        addChatMessage(chatbotMessage, false);
    }

    private String generateChatbotMessage(String userMessage) {
        // Implementation of ChatGPT API integration to generate chatbot response
        // This is just a placeholder method for demonstration purposes
        return "This is a response from the chatbot for the message: " + userMessage;
    }

    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
}
*/

