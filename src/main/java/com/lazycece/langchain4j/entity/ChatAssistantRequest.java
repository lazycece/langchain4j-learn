package com.lazycece.langchain4j.entity;

/**
 * AI 助手对话请求体
 */
public record ChatAssistantRequest(
        /** 会话标识，同一会话复用可保持对话上下文 */
        String memoryId,
        /** 用户消息 */
        String message
) {
}
