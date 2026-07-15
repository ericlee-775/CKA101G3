package com.farmily.ai.rag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.farmily.product.dto.ProductSummaryDTO;
import com.farmily.product.service.ProductService;

@Component
public class ProductIngestion implements ApplicationRunner {

    private final ProductService productService;
    private final VectorStore vectorStore;

    public ProductIngestion(ProductService productService, VectorStore vectorStore) {
        this.productService = productService;
        this.vectorStore = vectorStore;
    }

    // ApplicationRunner：Spring Boot 啟動完成後自動跑一次
    @Override
    public void run(ApplicationArguments args) {
        // 用 try-catch 包住：就算建庫失敗（例如 embedding 模型連不上），
        // 也只是 RAG 不能用，不會害整個網站起不來
        try {
            List<ProductSummaryDTO> products =
                    productService.getAllProducts(PageRequest.of(0, 1000)).getContent();

            List<Document> docs = products.stream()
                    .map(p -> {
                        // metadata 用 HashMap（Map.of 不允許 null 值，unit 可能為 null）
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("type", "商品");
                        meta.put("id", p.getProductId());
                        meta.put("price", p.getRetailPrice());
                        // 向量文字：目前只有商品名可用（沒有描述欄位）
                        return new Document(p.getProductName(), meta);
                    })
                    .toList();

            vectorStore.add(docs);   // 逐筆呼叫 embedding 轉向量，商品多會花幾秒
            System.out.println("【RAG】已灌入 " + docs.size() + " 筆商品到向量庫");
        } catch (Exception e) {
            // 印出完整錯誤，方便我們判斷 embedding 為什麼失敗
            System.err.println("【RAG】建庫失敗（不影響網站啟動）：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
