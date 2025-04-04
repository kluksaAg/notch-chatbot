package com.wearenotch.kluksa.notchchatbot.service;

import com.wearenotch.kluksa.notchchatbot.service.rag.RevenueTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class ChatbotService {

    private final ChatClient chatClient;

    public ChatbotService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String chatId, String userMessage) {
        return chatClient
            .prompt()
            .user(userMessage)
            .advisors(a -> a
                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50)
            ) // without memory advisor, function will not work
            .functions(FunctionCallback.builder()
                .function("getEarningsForYear", new RevenueTool())
                .description("Get the earnings for a given year")
                .inputType(RevenueTool.Request.class)
                .build())
            .call()
            .content();
    }
}
