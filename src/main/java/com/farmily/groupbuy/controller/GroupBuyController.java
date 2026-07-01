package com.farmily.groupbuy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.GroupBuyVO;
import com.farmily.groupbuy.service.GroupBuyService;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.service.ProductService;
@RestController
@RequestMapping("/api/groupBuy")
public class GroupBuyController {

	@Autowired
	GroupBuyService groupBuySvc;

	@Autowired
	ProductService productSvc;

	//給消費者看的
	@GetMapping("/{groupBuyId}")
	public ResponseEntity<GroupBuyVO>getOneGroupBuy
	(@PathVariable Integer groupBuyId){
		GroupBuyVO groupBuy=groupBuySvc.getOneGroupBuyId(groupBuyId);
		if(groupBuy!=null) {
			return ResponseEntity.ok(groupBuy);
		}else {
		return ResponseEntity.notFound().build();
}
	}
	
	@GetMapping("/all")
	public ResponseEntity<List<ProductGroupBuyDTO>> getAllGroupBuy() {
	    List<ProductGroupBuyDTO> list = groupBuySvc.getConsumerGroupBuyList();
	    return ResponseEntity.ok(list);
	}

	
	//等user寫好

	//團購主的發起請求
//	@PostMapping("/hostCreate")
//	public ResponseEntity<GroupBuyVO>createGroupBuy
//	(@RequestBody @Valid GroupBuyHostCreate hostCreate,
//	 @RequestParam Integer productId,HttpSession session		){
//		UserVO loginUser=(UserVO)session.getAttribute("userId");
//		Integer hostUserId=loginUser.getUserId();
//		groupBuySvc.hostRequest(hostCreate, productId, hostUserId)
//		return ResponseEntity.ok("成功送出申請");
//	}
	}

