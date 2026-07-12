package com.lazycece.langchain4j.controller;

import com.lazycece.langchain4j.entity.ChatAssistantRequest;
import com.lazycece.langchain4j.service.ChatAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 助手对话控制器，使用 @AiService 代理实现流式 SSE 响应
 */
@RestController
public class ChatAssistantController {

    private static final Logger log = LoggerFactory.getLogger(ChatAssistantController.class);

    /** SSE 超时时间：5 分钟 */
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;

    private final ChatAssistant chatAssistant;

    public ChatAssistantController(ChatAssistant chatAssistant) {
        this.chatAssistant = chatAssistant;
    }

    /**
     * 流式对话接口，通过 SSE 逐 token 推送回复
     *
     * @param request 包含 memoryId 和 message
     * @return SseEmitter 流式事件推送
     */
    @PostMapping(value = "/ai/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@RequestBody ChatAssistantRequest request) {
        // 参数校验
        if (request.memoryId() == null || request.memoryId().isBlank()) {
            SseEmitter errorEmitter = new SseEmitter();
            sendErrorAndComplete(errorEmitter, "会话ID不能为空");
            return errorEmitter;
        }
        if (request.message() == null || request.message().isBlank()) {
            SseEmitter errorEmitter = new SseEmitter();
            sendErrorAndComplete(errorEmitter, "消息不能为空");
            return errorEmitter;
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 调用 @AiService 代理获取流式输出
        chatAssistant.chat(request.memoryId(), request.message())
                .onPartialResponse(token -> {
                    // 逐 token 发送 SSE 事件
                    try {
                        emitter.send(SseEmitter.event().data(token));
                    } catch (Exception e) {
                        log.error("SSE 发送失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> {
                    // 发送完成标记
                    try {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("SSE 完成事件发送失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .onError(error -> {
                    // 异常处理
                    log.error("AI 对话调用失败", error);
                    try {
                        emitter.send(SseEmitter.event().data("[ERROR] " + error.getMessage()));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .start();

        return emitter;
    }

    /**
     * 发送错误事件并关闭 Emitter
     */
    private void sendErrorAndComplete(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event().data("[ERROR] " + errorMessage));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
