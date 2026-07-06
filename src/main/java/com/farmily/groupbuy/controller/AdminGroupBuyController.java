package com.farmily.groupbuy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.GroupBuyOrderDTO;
import com.farmily.groupbuy.model.GroupBuyParticipationVO;
import com.farmily.groupbuy.model.GroupBuyVO;
import com.farmily.groupbuy.service.GroupBuyService;
import com.farmily.product.service.ProductService;

@RestController
@RequestMapping("api/admin/groupBuy")
public class AdminGroupBuyController {
	@Autowired
	GroupBuyService groupBuySvc;

	@Autowired
	ProductService productSvc;

	// 給管理員看的總表
	@GetMapping("/list")
	public ResponseEntity<List<GroupBuyVO>> getGroupBuyForAdmin() {
		List<GroupBuyVO> list = groupBuySvc.showAllGroupBuyToAdmin();
		return ResponseEntity.ok(list);
	}
	
	//測試直接讓團購成團
	@PostMapping("/checkExpired") 
	public ResponseEntity<String> checkExpired() {
	groupBuySvc.checkExpiredGroupBuys(); return ResponseEntity.ok("團購結算完成"); }
	
	
	//給管理員看的訂單資料
	@GetMapping("/order/{groupBuyId}")
	public ResponseEntity<GroupBuyOrderDTO>geteach(@PathVariable Integer groupBuyId){
		GroupBuyOrderDTO groupBuy=groupBuySvc.showOrder(groupBuyId);
		return ResponseEntity.ok(groupBuy);
	}
	
	// 給管理員的單一筆當中的參與名單
	@GetMapping("/participation/{groupBuyId}")
	public ResponseEntity<GroupBuyParticipationVO> getOneGroupBuy(@PathVariable Integer groupBuyId) {
		GroupBuyParticipationVO groupBuy=groupBuySvc.getGroupBuyId(groupBuyId);
		if (groupBuy != null) {
			return ResponseEntity.ok(groupBuy);
		} else {
			return ResponseEntity.notFound().build();
		}

	}
}
