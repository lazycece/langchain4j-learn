package com.lazycece.langchain4j.controller;

import com.lazycece.langchain4j.entity.ChatRequest;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Chat 对话控制器，提供简单的文本对话接口
 */
@RestController
public class ChatController {

    private final ChatModel chatModel;

    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 简单对话接口，接收用户消息并返回模型回复
     *
     * @param request 用户消息
     * @return 模型回复
     */
    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        return chatModel.chat(request.message());
    }


}
