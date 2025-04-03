package com.wearenotch.kluksa.notchchatbot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Profile("init")
@Configuration
public class ETLConfig {

    private final VectorStore vectorStore;

    ETLConfig(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    void init() {
        final List<Document> documents = List.of("classpath:documents/racun.pdf", "classpath:documents/onborad.pdf").stream()
            .map(PagePdfDocumentReader::new)
            .map(PagePdfDocumentReader::get)
            .flatMap(List::stream)
            .toList();
        var textSplitter = new TokenTextSplitter();
        var transformedDocuments = textSplitter.apply(documents);
        vectorStore.add(transformedDocuments);
    }
}
