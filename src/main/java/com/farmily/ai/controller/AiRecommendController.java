package com.farmily.ai.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.product.dto.ProductDetailDTO;
import com.farmily.product.dto.ProductSummaryDTO;
import com.farmily.product.service.ProductService;

@RestController
public class AiRecommendController {

      private final ChatClient chatClient;
      private final ProductService productService;

      public AiRecommendController(ChatClient.Builder builder, ProductService productService) {
              this.chatClient = builder.build();
              this.productService = productService;
      }

      // ① AI 推薦：AI 只回 id，我自己抓數字、自己查真商品
      @GetMapping("/api/ai/recommend")
      public List<ProductDetailDTO> recommend(@RequestParam String need) {

              List<ProductSummaryDTO> products =
                              productService.getAllProducts(PageRequest.of(0, 20)).getContent();

              String menu = products.stream()
                              .map(p -> "id:" + p.getProductId()
                                              + " | " + p.getProductName()
                                              + " | NT$" + p.getRetailPrice())
                              .collect(Collectors.joining("\n"));

              // 叫 AI「只回 productId、逗號分隔」——小模型最擅長的簡單格式
              String raw = chatClient.prompt()
                              .user("商品清單：\n" + menu
                                              + "\n\n顧客需求：" + need
                                              + "\n從清單挑 2~3 樣最適合的，只回 productId，用逗號分隔，例如：3,7,12。"
                                              + "不要商品名、不要理由、不要其他任何文字。")
                              .call()
                              .content();

              // 從 AI 回的文字抓出所有數字（容錯：AI 多打字也只撈數字）
              List<Integer> ids = new ArrayList<>();
              Matcher m = Pattern.compile("\\d+").matcher(raw);
              while (m.find()) {
                      ids.add(Integer.parseInt(m.group()));
              }

              // 只留「清單裡真的存在」的 id（濾掉 AI 亂編、誤抓的價格數字）
              Set<Integer> validIds = products.stream()
                              .map(ProductSummaryDTO::getProductId)
                              .collect(Collectors.toSet());

              // 自己去 DB 撈真商品（價格/圖片以 DB 為準，AI 不經手）
              return ids.stream()
                              .distinct()
                              .filter(validIds::contains)
                              .map(productService::getProductDetail)
                              .filter(Objects::nonNull)
                              .toList();
      }

      // ② AI 問答：純文字，小模型最穩
      @GetMapping(value = "/api/ai/ask", produces = "text/plain;charset=UTF-8")
      public String ask(@RequestParam String q) {
              return chatClient.prompt()
                              .user("你是小農電商 Farmily 的親切客服，只回答農產品、購物相關問題。"
                                              + "請用繁體中文簡短回答：" + q)
                              .call()
                              .content();
      }
}