package com.farmily.groupbuy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.GroupBuyHostCreateDTO;
import com.farmily.groupbuy.model.GroupBuyReviewDTO;
import com.farmily.groupbuy.model.GroupBuyShowToUserJoinDTO;
import com.farmily.groupbuy.model.GroupBuyVO;
import com.farmily.groupbuy.service.GroupBuyService;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.service.ProductService;
import com.farmily.user.security.MemberUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groupBuy")
public class GroupBuyController {

	
	@Autowired
	GroupBuyService groupBuySvc;

	@Autowired
	ProductService productSvc;

	//消費者加入團購
	@PostMapping("/joinGroupBuy/{groupBuyId}")
	public ResponseEntity<String>joinGroupBuy
	(@RequestBody @Valid GroupBuyShowToUserJoinDTO join,
	 @RequestParam Integer productId,@RequestParam Integer groupBuyId, @AuthenticationPrincipal MemberUserDetails me){
		groupBuySvc.joinGroupBuy(join, groupBuyId, me.getUserId());
		return ResponseEntity.ok("參加完成");
	}
	
	
	// 給一般消費者看的單一團購頁面
	@GetMapping("/{groupBuyId}")
	public ResponseEntity<GroupBuyVO> getOneGroupBuy(@PathVariable Integer groupBuyId) {
		GroupBuyVO groupBuy = groupBuySvc.getOneGroupBuyId(groupBuyId);
		if (groupBuy != null) {
			return ResponseEntity.ok(groupBuy);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	//給管理員看的總表
	@GetMapping("/admin")
	public ResponseEntity<List<GroupBuyVO>>getGroupBuyForAdmin(){
		List<GroupBuyVO> list=groupBuySvc.showAllGroupBuyToAdmin(null);
		return ResponseEntity.ok(list);
	}

	// 一般消費者看的全部的可以團購清單
	@GetMapping("/all")
	public ResponseEntity<List<ProductGroupBuyDTO>> getAllGroupBuy() {
		List<ProductGroupBuyDTO> list = groupBuySvc.getConsumerGroupBuyList();
		return ResponseEntity.ok(list);
	}

	// 團購主的發起請求
	@PostMapping("/hostCreate")
	public ResponseEntity<String> createGroupBuy(@RequestBody @Valid GroupBuyHostCreateDTO hostCreate,
			@RequestParam Integer productId, @AuthenticationPrincipal MemberUserDetails me) {
		Integer hostUserId = me.getUserId();
		groupBuySvc.hostRequest(hostCreate, productId, hostUserId);
		return ResponseEntity.ok("申請完成");
	}
	
	

	// 小農審核團購
	@PostMapping("/farmerResponse/{groupBuyId}")
	public ResponseEntity<String> farmerResponse(@PathVariable Integer groupBuyId,
			@RequestBody @Valid GroupBuyReviewDTO farmerRes) {
		groupBuySvc.reviewGroupBuy(groupBuyId, farmerRes.getRequestStatus(), farmerRes.getRejectReason());
		return ResponseEntity.ok("審核完成");
	}

}
