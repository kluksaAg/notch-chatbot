package com.wearenotch.kluksa.notchchatbot.api;

import com.wearenotch.kluksa.notchchatbot.service.ChatbotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class ChatbotController {

    private final ChatbotService chatbotService;
    public record UserMessage(String text) {}

    ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat/{chatId}")
    public String chat(@PathVariable String chatId, @RequestBody UserMessage userMessage) {
        return chatbotService.chat(chatId, userMessage.text());
    }
}
