package com.wearenotch.kluksa.notchchatbot.service.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MyRetriever implements DocumentRetriever {

    private final TextToSqlRetriever textToSqlRetriever;
    private final ChatClient.Builder builder;
    private final VectorStoreDocumentRetriever vectorRetriever;

    public MyRetriever(TextToSqlRetriever textToSqlRetriever,
                       ChatClient.Builder builder,
                       VectorStore vectorStore) {
        this.textToSqlRetriever = textToSqlRetriever;
        this.builder = builder;
        this.vectorRetriever = VectorStoreDocumentRetriever.builder()
            .similarityThreshold(0.50)
            .vectorStore(vectorStore)
            .build();
    }

    @Override
    public List<Document> retrieve(Query query) {
        final List<Document> documents = vectorRetriever.retrieve(query);
        final List<Document> compressedDocuments = new ArrayList<>(documents);
        final List<Document> retrieve = textToSqlRetriever.retrieve(query);
        final List<Document> list = retrieve.stream()
            .collect(Collectors.toMap(Document::getText, Function.identity(), (d1, d2) -> d1))
            .entrySet().stream().map(Map.Entry::getValue)
            .toList();

        compressedDocuments.addAll(list);
        return compressedDocuments;
    }

}
