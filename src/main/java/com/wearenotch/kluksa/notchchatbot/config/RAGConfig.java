package com.wearenotch.kluksa.notchchatbot.config;

import com.wearenotch.kluksa.notchchatbot.service.rag.MyRetriever;
import com.wearenotch.kluksa.notchchatbot.service.rag.TextToSqlRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class RAGConfig {
    private final ChatClient.Builder chatClientBuilder;
    private final MyRetriever myRetriever;

    public RAGConfig(ChatClient.Builder chatClientBuilder, MyRetriever myRetriever) {
        this.chatClientBuilder = chatClientBuilder;
        this.myRetriever = myRetriever;
    }

    @Primary
    @Bean(name = "vectorStoreAdviser")
    public RetrievalAugmentationAdvisor getAdvisor() {
        return RetrievalAugmentationAdvisor.builder()
            .documentRetriever(myRetriever)
            .queryExpander(queryExpander())
            .queryTransformers(queryTransformers())
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

    private List<QueryTransformer> queryTransformers() {
        final PromptTemplate rewritePrompt = new PromptTemplate("""
            Given a user query, rewrite it to provide better results when querying a {target}.
            Remove any irrelevant information, and ensure the query is concise and specific. 
            
            Original query:
            {query}
            
            Rewritten query:
        """);

        final RewriteQueryTransformer rewriteQueryTransformer = RewriteQueryTransformer.builder()
            .chatClientBuilder(chatClientBuilder)
            .promptTemplate(rewritePrompt)
            .build();
        return List.of(rewriteQueryTransformer);
    }
}
