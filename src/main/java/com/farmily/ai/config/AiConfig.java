package com.farmily.ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.farmily.ai.memory.RedisChatMemoryRepository;

@Configuration
public class AiConfig {
	 @Bean
	 public ChatMemory chatMemory(RedisChatMemoryRepository redisRepo) {
	     return MessageWindowChatMemory.builder()
	            .chatMemoryRepository(redisRepo)   // ← 記憶存 Redis，不是預設的記憶體
	            .maxMessages(5)                   // ← 只帶最近 5 則給 AI
	            .build();
	    }
	 @Bean
	 public VectorStore vectorStore(EmbeddingModel embeddingModel) {
	     // SimpleVectorStore = 記憶體向量庫。embeddingModel 由 Ollama starter 自動注入
	     return SimpleVectorStore.builder(embeddingModel).build();
	 }
}
