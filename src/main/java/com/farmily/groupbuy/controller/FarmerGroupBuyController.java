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
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.GroupBuyFarmerDTO;
import com.farmily.groupbuy.model.GroupBuyOrderDTO;
import com.farmily.groupbuy.model.GroupBuyReviewDTO;
import com.farmily.groupbuy.service.GroupBuyService;
import com.farmily.product.service.ProductService;
import com.farmily.user.security.FarmerUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/farmer/groupBuy")
public class FarmerGroupBuyController {

	@Autowired
	GroupBuyService groupBuySvc;

	@Autowired
	ProductService productSvc;
	
	//小農前台的顯示團購清單
	@GetMapping("/list")
	public ResponseEntity<List<GroupBuyFarmerDTO>>farmerGroupBuyList
	( @AuthenticationPrincipal FarmerUserDetails  me){
		List<GroupBuyFarmerDTO>list=groupBuySvc.showGroupBuyList(me.getFarmerId());
		return ResponseEntity.ok(list);
	}
	
	//小農前台顯示的單筆訂單詳細資料
	@GetMapping("/order/{groupBuyId}")
	public ResponseEntity<GroupBuyOrderDTO>geteach(@PathVariable Integer groupBuyId){
		GroupBuyOrderDTO groupBuy=groupBuySvc.showOrder(groupBuyId);
		return ResponseEntity.ok(groupBuy);
	}
	
	//小農前台的團購審核
	@PostMapping("/farmerResponse/{groupBuyId}")
	public ResponseEntity<String> farmerResponse(
	        @PathVariable Integer groupBuyId,
	        @RequestBody @Valid GroupBuyReviewDTO farmerRes,
	        @AuthenticationPrincipal FarmerUserDetails  me) {
	    groupBuySvc.reviewGroupBuy(
	            groupBuyId,me.getFarmerId(),
	            farmerRes.getRequestStatus(),
	            farmerRes.getRejectReason()
	    );
	    return ResponseEntity.ok("審核完成");
	}
}
