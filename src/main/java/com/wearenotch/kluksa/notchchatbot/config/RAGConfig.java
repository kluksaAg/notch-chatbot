package com.wearenotch.kluksa.notchchatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGConfig {
    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    public RAGConfig(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClientBuilder = chatClientBuilder;
    }

    @Bean
    public RetrievalAugmentationAdvisor getAdvisor() {
        return RetrievalAugmentationAdvisor.builder()
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .build())
            .queryExpander(queryExpander())
            .queryAugmenter(ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build())
            .build();
    }

    private QueryExpander queryExpander() {
        String templateString = """
            You are an expert at information retrieval and search optimization.
            Your task is to generate {number} different versions of the given query.
            
            If the original query is composed of multiple questions, put each question in a separate variant.
            Each variant must cover different perspectives or aspects of the topic,
            while maintaining the core intent of the original query. The goal is to
            expand the search space and improve the chances of finding relevant information.
            
            Do not explain your choices or add any other text.
            Provide the query variants separated by newlines.
            
            Original query: {query}
            
            Query variants:
            """;

        return MultiQueryExpander.builder()
            .chatClientBuilder(chatClientBuilder)
            .includeOriginal(true)
            .promptTemplate(new PromptTemplate(templateString))
            .numberOfQueries(4)
            .build();
    }
}
