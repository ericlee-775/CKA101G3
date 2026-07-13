package com.farmily.groupbuy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.WishListDTO;
import com.farmily.groupbuy.service.GroupBuyService;
import com.farmily.user.security.MemberUserDetails;

@RestController
@RequestMapping("/api/member/groupBuyLists")
public class GroupBuyWishlistController {
	
	@Autowired
	private GroupBuyService groupBuySvc;
	
	//加單筆團購到我的收藏底下的團購
	@PostMapping("/{groupBuyId}")
	public ResponseEntity<Void> addGroupBuyWishlist(@PathVariable Integer groupBuyId,
	        										@AuthenticationPrincipal MemberUserDetails me) {
		groupBuySvc.groupBuyFavorite(me.getUserId(),groupBuyId);
	    return ResponseEntity.ok().build();
	}
	
	
	//移除單筆團購收藏
	@DeleteMapping("/{groupBuyId}")
	public ResponseEntity<Void> deleteGroupBuyWishlist(@PathVariable Integer groupBuyId,
													   @AuthenticationPrincipal MemberUserDetails me) {
		groupBuySvc.removeGroupBuyFavorite(me.getUserId(),groupBuyId);
	    return ResponseEntity.ok().build();
	}
	
	//查看團購收藏
	@GetMapping
	public ResponseEntity<List<WishListDTO>> showAllMyGroupBuyWishList(@AuthenticationPrincipal MemberUserDetails me){
		List<WishListDTO> list= groupBuySvc.showGroupBuyFavoriteList(me.getUserId());
		return ResponseEntity.ok(list);
	}

}
