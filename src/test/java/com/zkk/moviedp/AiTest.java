package com.zkk.moviedp;

import com.zkk.moviedp.assistant.MovieAgent;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiTest {

    @Test
    public void testAi() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
        String answer = model.chat("你是谁？");
        System.out.println(answer);
    }

    @Autowired
    private OpenAiChatModel openAiChatModel;
    @Test
    public void testOpenAi() {
        String ans = openAiChatModel.chat("你是谁");
        System.out.println(ans);
    }

    @Autowired
    private MovieAgent movieAgent;
    @Test
    public void testAiAgent() {
        String ans1 = movieAgent.chat(1, "我是花花");
        System.out.println(ans1);
        String ans2 = movieAgent.chat(1, "我是谁");
        System.out.println(ans2);
        String ans3 = movieAgent.chat(2, "我是谁");
        System.out.println(ans3);
    }

    @Test
    public void testAiTools() {
        String ans = movieAgent.chat(3, "1+22等于多少");
        System.out.println(ans);
    }

    @Test
    public void testRAG() {
        Document document = FileSystemDocumentLoader.loadDocument("C:\\Users\\朱凯凯\\Desktop\\后端分享会笔记.txt");
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
        System.out.println(embeddingStore);
    }
}
