package com.lazycece.langchain4j.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatMemory 配置，为每个会话提供独立的滑动窗口记忆
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 注册 ChatMemoryStore Bean，底层用 ConcurrentHashMap 存储各会话的消息列表
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        // key 为 memoryId，value 为该会话的对话消息列表
        Map<Object, List<ChatMessage>> store = new ConcurrentHashMap<>();
        return new ChatMemoryStore() {
            @Override
            public List<ChatMessage> getMessages(Object memoryId) {
                List<ChatMessage> list = store.get(memoryId);
                return list == null ? new ArrayList<>() : list;
            }

            @Override
            public void updateMessages(Object memoryId, List<ChatMessage> messages) {
                store.put(memoryId, new ArrayList<>(messages));
            }

            @Override
            public void deleteMessages(Object memoryId) {
                store.remove(memoryId);
            }
        };
    }

    /**
     * 注册 ChatMemoryProvider Bean，为每个 memoryId 创建滑动窗口记忆（保留最近 10 条消息）
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }

}
