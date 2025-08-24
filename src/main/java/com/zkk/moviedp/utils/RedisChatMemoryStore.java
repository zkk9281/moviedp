package com.zkk.moviedp.utils;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String value = redisTemplate.opsForValue().get("chat:" + memoryId.toString());
        if(value == null || value.isEmpty()){
            return List.of();
        }
        return ChatMessageDeserializer.messagesFromJson(value);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String messages = ChatMessageSerializer.messagesToJson(list);
        redisTemplate.opsForValue().set("chat:" + memoryId.toString(), messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete("chat:" + memoryId.toString());
    }
}
