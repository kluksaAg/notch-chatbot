package com.wearenotch.kluksa.notchchatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(@Value("classpath:/prompts/system-prompt.st") Resource systemPromptResource,
                                 ChatClient.Builder chatClientBuilder,
                                 @Qualifier("vectorStoreAdviser") RetrievalAugmentationAdvisor ragAdvisor) {
        return chatClientBuilder
            .defaultAdvisors(
                ragAdvisor,
//                MessageChatMemoryAdvisor.builder(chatMemory()).build(),
                new SimpleLoggerAdvisor())
            .defaultSystem(systemPromptResource)
            .build();
    }

    @Bean
    ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
