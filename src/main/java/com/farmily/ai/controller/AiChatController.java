package com.farmily.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.user.security.MemberUserDetails;   // 你們登入身分的型別（路徑依實際調整）

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = builder
                // 兩個顧問一起掛：①記憶顧問撈歷史、②RAG顧問撈相關商品，都自動塞進 prompt
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().topK(4).build())
                                .build())
                // 全站客服的人設，講一次即可，每次呼叫自動帶
                .defaultSystem("你是小農電商 Farmily 的親切烏龜客服小龜，只回答農產品與購物相關問題，"
                        + "請一律用繁體中文簡短回答，語氣親切可愛。")
                .build();
    }

    @GetMapping(value = "/api/ai/chat", produces = "text/plain;charset=UTF-8")
    public String chat(@RequestParam String message,
                       @AuthenticationPrincipal MemberUserDetails me,
                       HttpServletRequest request) {

        // ★ 安全核心：conversationId 由「後端身分」決定，前端無法偽造
        String conversationId = (me != null)
                ? "member-" + me.getUserId()          // 登入者：綁會員 id
                : "guest-" + request.getSession(true).getId();  // 訪客：綁 session

        return chatClient.prompt()
                .user(message)
                // 告訴記憶顧問：這次對話屬於哪個 conversationId
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}