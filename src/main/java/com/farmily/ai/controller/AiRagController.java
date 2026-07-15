package com.farmily.ai.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiRagController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public AiRagController(VectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
    }

    // 只測檢索：看看用一句話能撈回哪些商品（還沒接 AI 生成）
    @GetMapping("/api/ai/search")
    public List<String> search(@RequestParam String q) {
        List<Document> hits = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(5).build());
        // 回傳商品名 + 相似度分數，方便肉眼檢查撈得準不準
        return hits.stream()
                .map(d -> d.getText() + "  (score=" + d.getScore() + ")")
                .toList();
    }

    // 完整 RAG：檢索 + AI 生成（QuestionAnswerAdvisor 自動做「查向量→塞prompt」）
    @GetMapping(value = "/api/ai/rag", produces = "text/plain;charset=UTF-8")
    public String rag(@RequestParam String q) {
        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(4).build())
                        .build())
                .user(q)
                .call()
                .content();
    }
}
