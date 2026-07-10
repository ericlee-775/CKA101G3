package com.farmily.groupbuy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.GroupBuyDetailDTO;
import com.farmily.groupbuy.model.GroupBuyStatus;
import com.farmily.groupbuy.service.GroupBuyService;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.service.ProductService;


@RestController
@RequestMapping("api/groupBuy")
public class PublicGroupBuyController {
	@Autowired
	GroupBuyService groupBuySvc;

	@Autowired
	ProductService productSvc;
	
	// 給一般消費者看的單一團購頁面
	@GetMapping("/{groupBuyId}")
	public ResponseEntity<GroupBuyDetailDTO> getOneGroupBuy(@PathVariable Integer groupBuyId) {
	    GroupBuyDetailDTO dto = groupBuySvc.getOneGroupBuyDetail(groupBuyId);
	    return ResponseEntity.ok(dto);
	}
	
	// 一般消費者看的全部的可以團購清單
	//這邊要濾掉被停權小農的相關商品
	@GetMapping("/consumer/list")
	public ResponseEntity<List<ProductGroupBuyDTO>> getConsumerGroupBuyList(
	        @RequestParam(defaultValue = "all") String type) {
	    if ("available".equals(type)) {
	        return ResponseEntity.ok(productSvc.getAllGroupBuyProducts());
	    }
	    List<GroupBuyStatus> statuses = switch (type) {
	        case "open" -> List.of(GroupBuyStatus.open);
	        default -> List.of(GroupBuyStatus.pending, GroupBuyStatus.open);
	    };
	    return ResponseEntity.ok(groupBuySvc.getConsumerGroupBuyList(statuses));
	}
	@PostMapping("/checkExpired") 
	public ResponseEntity<String> checkExpired() {
	groupBuySvc.checkExpiredGroupBuys();
	return ResponseEntity.ok("團購結算完成"); }
	
	
}
