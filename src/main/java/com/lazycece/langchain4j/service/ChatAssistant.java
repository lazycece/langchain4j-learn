package com.lazycece.langchain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * AI 对话助手接口，使用 @AiService 注解自动生成代理实现
 */
@AiService
public interface ChatAssistant {

    /**
     * 流式对话，支持多轮上下文记忆
     *
     * @param memoryId 会话标识，同一会话复用可保持上下文
     * @param message  用户消息
     * @return 流式 Token 输出
     */
    @SystemMessage("你是一个乐于助人的AI助手，回答简洁、准确、友好。")
    TokenStream chat(@MemoryId String memoryId, @UserMessage String message);
}
