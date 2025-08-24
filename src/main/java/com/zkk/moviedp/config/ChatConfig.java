package com.zkk.moviedp.config;

import com.zkk.moviedp.utils.RedisChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ChatConfig {

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        return memoryId -> MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
    }

    @Bean
    public ContentRetriever myContentRetriever() {
        Document document1 = FileSystemDocumentLoader.loadDocument("D:\\Data\\IdeaProjects\\moviedp\\moviedp\\src\\main\\resources\\movies.txt");
        List<Document> documents = Arrays.asList(document1);
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }
}
