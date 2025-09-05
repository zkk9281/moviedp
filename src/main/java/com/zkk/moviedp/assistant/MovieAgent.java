package com.zkk.moviedp.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService(
        chatMemoryProvider = "chatMemoryProvider",
        tools = {"movieController", "blogController", "showingServiceImpl"},
        contentRetriever = "movieContentRetriever"
)
public interface MovieAgent {

    @SystemMessage(fromResource = "my-prompt-template.txt")
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
