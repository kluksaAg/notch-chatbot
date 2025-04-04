package com.wearenotch.kluksa.notchchatbot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.stream.Stream;

@Profile("init")
@Configuration
public class ETLConfig {

    private final VectorStore vectorStore;

    ETLConfig(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    void init() {
        final var documents = Stream.of("classpath:documents/racun.pdf", "classpath:documents/onborad.pdf")
            .map(PagePdfDocumentReader::new)
            .map(PagePdfDocumentReader::get)
            .flatMap(List::stream)
            .toList();
        var textSplitter = new TokenTextSplitter();
        var transformedDocuments = textSplitter.apply(documents);
        vectorStore.add(transformedDocuments);
    }
}
