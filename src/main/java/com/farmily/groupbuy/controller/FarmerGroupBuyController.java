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
import com.farmily.groupbuy.model.GroupBuyParticipationDTO;
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
	
	//小農前台顯示的單筆團購請求詳細內容
	@GetMapping("/list/{groupBuyId}")
	public ResponseEntity<GroupBuyFarmerDTO> oneFarmerGroupBuy(
	        @PathVariable Integer groupBuyId,
	        @AuthenticationPrincipal FarmerUserDetails me) {
	    GroupBuyFarmerDTO dto =
	            groupBuySvc.getOneGroupBuyForFarmer(groupBuyId, me.getFarmerId());
	    return ResponseEntity.ok(dto);
	}
	//顯示訂單總覽
	@GetMapping("/orderList")
	public ResponseEntity<List<GroupBuyOrderDTO>>orderList(@AuthenticationPrincipal FarmerUserDetails me){
		List<GroupBuyOrderDTO> groupBuyOrder=groupBuySvc.showOrderList(me.getFarmerId());
		return ResponseEntity.ok(groupBuyOrder);
	}
	//小農的團購總收益
	@GetMapping("/totalRevenue")
	public ResponseEntity<Long>showGroupBuyTotalRevenue(@AuthenticationPrincipal FarmerUserDetails me){
		Long total=groupBuySvc.getGroupBuyTotalRevenue(me.getFarmerId());
		return ResponseEntity.ok(total);
	}
	
	//小農看的單筆開團中的進度
	@GetMapping("/progress/{groupBuyId}")
	public ResponseEntity<GroupBuyParticipationDTO>showOneGroupBuyProgress
	(		@PathVariable Integer groupBuyId,
			@AuthenticationPrincipal FarmerUserDetails me){
		GroupBuyParticipationDTO p=groupBuySvc.showGroupBuyProgress(groupBuyId, me.getFarmerId());
		return ResponseEntity.ok(p);
	}
	
	
	
	
	//小農前台顯示的單筆訂單詳細資料
	@GetMapping("/order/{orderId}")
	public ResponseEntity<GroupBuyOrderDTO>geteach(@PathVariable Integer orderId){
		GroupBuyOrderDTO order=groupBuySvc.showOneOrder(orderId);
		return ResponseEntity.ok(order);
	}
	
	//小農前台出貨狀態更新
	@PostMapping("/orderShipped/{orderId}")
	public ResponseEntity<String>shipped(
			@PathVariable Integer orderId,
			@RequestBody @Valid GroupBuyOrderDTO farmerRes,
			@AuthenticationPrincipal FarmerUserDetails me){
		groupBuySvc.ship(orderId,me.getFarmerId());
		return ResponseEntity.ok("更新完成");
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
