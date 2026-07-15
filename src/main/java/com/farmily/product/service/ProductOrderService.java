package com.farmily.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.farmily.product.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.coupon.dto.MyCouponDTO;
import com.farmily.coupon.model.CouponStatus;
import com.farmily.coupon.service.CouponDetailService;
import com.farmily.notification.service.NotificationService;
import com.farmily.product.dto.ProductOrderCheckoutInfoDTO;
import com.farmily.product.dto.ProductOrderCheckoutItemDTO;
import com.farmily.product.dto.ProductOrderFarmerGroupDTO;
import com.farmily.product.dto.ProductOrderFarmerResponseDTO;
import com.farmily.product.dto.ProductOrderItemFarmerResponseDTO;
import com.farmily.product.dto.ProductOrderItemResponseDTO;
import com.farmily.product.dto.ProductOrderMemberGroupDTO;
import com.farmily.product.dto.ProductOrderRequestDTO;
import com.farmily.product.dto.ProductOrderResponseDTO;
import com.farmily.shoppingcart.dto.ShoppingcartDTO;
import com.farmily.shoppingcart.service.ProductShoppingCartService;
import com.farmily.user.dto.CityDistrictResponse;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.User;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.FarmerRepository;

@Service
public class ProductOrderService {

	private static final int PAGE_SIZE = 5;
	
	@Autowired
	private ProductOrderRepository orderRepo;

	@Autowired
	private ProductOrderItemRepository orderItemRepo;

	@Autowired
	private ProductShoppingCartService cartSvc;

	@Autowired
	private NotificationService nSvc;
	
	@Autowired
	private ProductRepository prodRepo;

	@Autowired
	private CityDistrictRepository cityDistrictRepo;
	
	@Autowired
	private CouponDetailService couponSvc;

	@Autowired
	private FarmerRepository farmerRepo;


	// 小農更新訂單出貨狀態
	@Transactional
	public void updateShippedStatus(Integer farmerId, Integer orderId) {
		
		// 取得此筆訂單中屬於該小農的明細
		List<ProductOrderItemVO> items = orderItemRepo.findByOrder_OrderIdAndFarmerIdOrderByOrderItemIdDesc(orderId, farmerId);
		
		if (items.isEmpty()) {
			throw new AccessDeniedException("此訂單沒有您的商品");
		}
		
		LocalDateTime now = LocalDateTime.now();
		
		for (ProductOrderItemVO i : items) {
			if (i.getShippedStatus() != ShippedStatus.pending) {
				throw new IllegalStateException("訂單已出貨，無法再次操作");
			}
			
			i.setShippedStatus(ShippedStatus.shipping);
			i.setShippedAt(now);
		}
	}
	
	
	// 會員確認收貨 (現階段設定: 消費者確認收貨同時更新 shipped_status, received_at, payout_status)
	// 按一次確認收貨 = 收到該小農農場的全部商品 (不是按品項確認，且 payoutStatus 也可以一次更新)
	// order_status, completed_at 需先檢查該小農的訂單是否都確認收貨
	@Transactional
	public void updateReceived(Integer userId, Integer orderId, Integer farmerId) {
		
		// 取得這筆訂單
		ProductOrderVO order = orderRepo.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("查無此訂單"));
		
		// 檢查訂單是否屬於此會員
		if (!(order.getUserId().equals(userId))) {
			throw new AccessDeniedException("無權限操作此訂單");
		}
		
		// 篩選出這筆訂單中該小農的品項明細
		List<ProductOrderItemVO> items = order.getItems().stream()
				.filter(p -> p.getFarmerId().equals(farmerId)).toList();
		
		if (items.isEmpty()) {
			throw new IllegalArgumentException("此訂單沒有該小農的商品");
		}
		
		LocalDateTime now = LocalDateTime.now();
		
		for (ProductOrderItemVO i : items) {
			
			// 檢查品項是否已出貨
			if (i.getShippedStatus() == ShippedStatus.pending) {
				throw new IllegalStateException("尚未出貨，無法確認收貨");
			}
			if (i.getShippedStatus() == ShippedStatus.delivered) {
				throw new IllegalStateException("訂單已確認貨，無法再次操作");
			}
			
			i.setShippedStatus(ShippedStatus.delivered);
			i.setReceivedAt(now);
			i.setPayoutStatus(PayoutStatus.paid);
		}

		// 檢查此筆訂單是否已全部確認收貨，更新整筆訂單狀態
		boolean allReceived = order.getItems().stream().allMatch(p -> p.getReceivedAt() != null);
		if (allReceived) {
			order.setOrderStatus(OrderStatus.completed);
			order.setCompletedAt(now);
		}
			
	}

	// 取得會員訂單列表
	@Transactional (readOnly = true)
	public Page<ProductOrderResponseDTO> getOrderByUser(Integer userId, int page){
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("orderId")));
		Page<ProductOrderVO> list = orderRepo.findByUserId(userId, pageable);
		Page<ProductOrderResponseDTO> dtoList = list.map(this::toOrderDTO);
		return dtoList;
	}


	// 取得會員訂單明細 (按小農分組)
	@Transactional (readOnly = true)
	public List<ProductOrderMemberGroupDTO> getOrderItems(Integer userId, Integer orderId){
		ProductOrderVO order = orderRepo.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("查無此訂單"));
		
		// 檢查訂單是否屬於此會員
		if (!(order.getUserId().equals(userId))) {
			throw new AccessDeniedException("無權限查看此訂單");
		}
		
		// 取明細
		List<ProductOrderItemVO> items = orderItemRepo.findByOrder_OrderIdOrderByOrderItemIdDesc(orderId);
		
		// 按小農 id 分組
		Map<Integer, List<ProductOrderItemVO>> byFarmer = items.stream()
				.collect(Collectors.groupingBy(ProductOrderItemVO::getFarmerId, LinkedHashMap::new, Collectors.toList()));
		
		// 查農場名稱
		Set<Integer> farmerIds = byFarmer.keySet();
		Map<Integer, String> farmerNames = new HashMap<>();
		for (Farmer f : farmerRepo.findAllById(farmerIds)) {
			farmerNames.put(f.getFarmerId(), f.getFarmName());
		}
		
		List<ProductOrderMemberGroupDTO> groupList = new ArrayList<>();
		for (Map.Entry<Integer, List<ProductOrderItemVO>> e : byFarmer.entrySet()) {
			Integer farmerId = e.getKey();
			List<ProductOrderItemVO> groupItem = e.getValue();
			
			ProductOrderMemberGroupDTO g = new ProductOrderMemberGroupDTO();
			g.setFarmerId(farmerId);
			g.setFarmerName(farmerNames.get(farmerId));
			
			g.setShippedStatus(groupItem.get(0).getShippedStatus().name());
			g.setShippedAt(groupItem.get(0).getShippedAt());
			g.setReceivedAt(groupItem.get(0).getReceivedAt());
			g.setSubtotal(groupItem.stream().mapToInt(i -> i.getPrice() * i.getQuantity()).sum());
			g.setItems(groupItem.stream().map(this::toOrderItemDTO).toList());
			
			groupList.add(g);
		}

		return groupList;
	}

	
	// 取得小農訂單列表 + 明細
	@Transactional (readOnly = true)
	public Page<ProductOrderFarmerResponseDTO> getOrderByFarmer(Integer farmerId, int page){
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("orderId")));
		Page<ProductOrderVO> list = orderRepo.findOrdersByFarmerId(farmerId, pageable);
		Page<ProductOrderFarmerResponseDTO> dtoList = list.map(vo -> toFarmerOrderDTO(vo, farmerId));
		
		return dtoList;
	}
	
		
	// 把 Repository 查回來的 orderVO 轉成 orderDTO
	private ProductOrderResponseDTO toOrderDTO(ProductOrderVO vo){
		ProductOrderResponseDTO dto = new ProductOrderResponseDTO();
		dto.setCreatedAt(vo.getCreatedAt());
		dto.setOrderId(vo.getOrderId());
		dto.setTotalAmount(vo.getTotalAmount());
		dto.setDiscountAmount(vo.getDiscountAmount());
		dto.setFinalPayment(vo.getFinalPayment());

		return dto;
	}

	// 把 Repository 查回來的 itemVO 轉成 itemDTO
	private ProductOrderItemResponseDTO toOrderItemDTO(ProductOrderItemVO vo){
		ProductOrderItemResponseDTO dto = new ProductOrderItemResponseDTO();
		dto.setProductName(vo.getProductName());
		dto.setProductId(vo.getProductId());
		dto.setPrice(vo.getPrice());
		dto.setQuantity(vo.getQuantity());
		dto.setItemSubtotal(vo.getPrice() * vo.getQuantity());

		return dto;
	}

		
	// 把 Repository 查回來的 orderVO 轉成小農 orderDTO
	private ProductOrderFarmerResponseDTO toFarmerOrderDTO(ProductOrderVO vo, Integer farmerId) {
		ProductOrderFarmerResponseDTO dto = new ProductOrderFarmerResponseDTO();
		dto.setOrderId(vo.getOrderId());
		dto.setUserId(vo.getUserId());
		dto.setShippingAddress(vo.getShippingAddress());
		dto.setCreatedAt(vo.getCreatedAt());
		
		List<ProductOrderItemVO> items = orderItemRepo.findByOrder_OrderIdAndFarmerIdOrderByOrderItemIdDesc(vo.getOrderId(), farmerId);
		
		dto.setItems(items.stream().map(this::toFarmerOrderItemDTO).toList());
		
		// 計算該小農的訂單小計
		int subtotal = items.stream()
				.mapToInt(item -> item.getPrice() * item.getQuantity())
				.sum();
		
		dto.setSubtotal(subtotal);
		dto.setShippedStatus(items.get(0).getShippedStatus().name());
		dto.setShippedAt(items.get(0).getShippedAt());
		dto.setPayoutStatus(items.get(0).getPayoutStatus().name());
		
		return dto;
	}
	
	// 把 Repository 查回來的 itemVO 轉成小農 itemDTO
	private ProductOrderItemFarmerResponseDTO toFarmerOrderItemDTO(ProductOrderItemVO vo){
		ProductOrderItemFarmerResponseDTO dto = new ProductOrderItemFarmerResponseDTO();
		dto.setProductName(vo.getProductName());
		dto.setProductId(vo.getProductId());
		dto.setPrice(vo.getPrice());
		dto.setQuantity(vo.getQuantity());

		return dto;
	}
	

	// 取得購物車商品，並撈回當下商品資訊
	private List<ShoppingcartDTO> loadCart(Integer userId){ 
		List<ShoppingcartDTO> cartItems = cartSvc.getcart(userId);
		if (cartItems == null || cartItems.isEmpty()) {
			throw new IllegalStateException("購物車為空");
		}
		return cartItems;
	}
	
	// 一次撈回所有商品各自的詳細資料 (現階段先使用 VO，之後再加 ProductOrderDTO)
	private Map<Integer, ProductVO> loadProducts(List<ShoppingcartDTO> cartItems){
		
		// 取得購物車中有什麼商品 (每個 cartItems 取出 productId)
		List<Integer> prodIds = cartItems.stream().map(ShoppingcartDTO::getProductId).toList();
		
		// 用 Ids 一次撈回所有商品各自的詳細資料 (現階段先使用 VO，之後再加 ProductOrderDTO)
		// 需要的資料: retailPrice, farmerId, status, productName
		List<ProductVO> prods = prodRepo.findAllById(prodIds);
		
		// 建一個 Map 把購物車內有的商品 Id 對應它的商品詳情，之後可用 key(id) 帶出商品資料
		Map<Integer, ProductVO> prodMap = new HashMap<>();
		for (ProductVO prod : prods) {
			prodMap.put(prod.getProductId(), prod);
		}
		
		// 檢查商品上架狀態 (比對購物車的 productId 和建好的商品 VO Map)，並重查一次當下商品價格
		for (ShoppingcartDTO ci : cartItems) {
			ProductVO prod = prodMap.get(ci.getProductId());
			if (prod == null) {
				throw new IllegalStateException("商品不存在" + ci.getProductId());
			}
			if (prod.getStatus() != ProductStatus.ACTIVE) {
				throw new IllegalStateException("商品已下架" + ci.getProductId());
			}
		}
		
		return prodMap;
	}
	

	// 取得 checkout-info 訂單預覽頁
	@Transactional (readOnly = true)
	public ProductOrderCheckoutInfoDTO getCheckoutInfo(User u) {
		Integer userId = u.getUserId();
		ProductOrderCheckoutInfoDTO dto = new ProductOrderCheckoutInfoDTO();
		
		// 取得訂購資訊
		dto.setUserName(u.getUserName());
		dto.setPhone(u.getUserPhoneNum());
		if (u.getCityDistrict() != null) {
			dto.setDistrict(CityDistrictResponse.from(u.getCityDistrict()));
		}
		dto.setDetailAddress(u.getUserAddress());
		
		// 讀購物車商品
		List<ShoppingcartDTO> cartItems = loadCart(userId);

		// 建一個 Map 把購物車內有的商品 Id 對應它的商品詳情，之後可用 key(id) 帶出商品資料
		Map<Integer, ProductVO> prodMap = loadProducts(cartItems);

		
		// 找 farmerName
		List<Integer> farmerIds = prodMap.values().stream()
				.map(ProductVO::getFarmerId).distinct().toList();
		
		List<Farmer> farmers = farmerRepo.findAllById(farmerIds);
		Map<Integer, String> farmerNames = new HashMap<>();
		for (Farmer f : farmers) {
			farmerNames.put(f.getFarmerId(), f.getFarmName());
		}
		
		int totalAmount = 0;
		
		// 依 farmerId 分組
		Map<Integer, List<ProductOrderCheckoutItemDTO>> byFarmer = new HashMap<>();
		
		for (ShoppingcartDTO ci : cartItems) {
			ProductVO prod = prodMap.get(ci.getProductId());

			// 記錄此商品的明細
			ProductOrderCheckoutItemDTO orderItem = new ProductOrderCheckoutItemDTO();
			orderItem.setProductId(prod.getProductId());
			orderItem.setProductName(prod.getProductName());
			orderItem.setQuantity(ci.getQuantity());
			orderItem.setPrice(prod.getRetailPrice());
			orderItem.setItemSubtotal(prod.getRetailPrice() * ci.getQuantity());

			totalAmount += prod.getRetailPrice() * ci.getQuantity();
			
			if (byFarmer.containsKey(prod.getFarmerId())) {
				byFarmer.get(prod.getFarmerId()).add(orderItem);
			}
			else {
				List<ProductOrderCheckoutItemDTO> list = new ArrayList<>();
				list.add(orderItem);
				byFarmer.put(prod.getFarmerId(), list);
			}
		}
		
		// 把 byFarmer 轉成前端顯示用的 List<ProductOrderFarmerGroupDTO>
		List<ProductOrderFarmerGroupDTO> farmerGroup = new ArrayList<>();
		for (Map.Entry<Integer, List<ProductOrderCheckoutItemDTO>> e : byFarmer.entrySet()) {
			ProductOrderFarmerGroupDTO g = new ProductOrderFarmerGroupDTO();
			g.setFarmerId(e.getKey());
			g.setFarmerName(farmerNames.get(e.getKey()));
			g.setItems(e.getValue());
			g.setSubtotal(e.getValue().stream().mapToInt(ProductOrderCheckoutItemDTO::getItemSubtotal).sum());
			farmerGroup.add(g);
		}
		dto.setFarmerGroup(farmerGroup);
		dto.setTotalAmount(totalAmount);
		
				
		// 取得可用的優惠券
		int total = totalAmount;
		
		List<MyCouponDTO> usableCoupons = couponSvc.getMyCoupons(userId).stream()
				.filter(c -> c.getStatus() == CouponStatus.UNUSED)
				.filter(c -> c.getIssueEndDate() == null || !LocalDateTime.now().isAfter(c.getIssueEndDate()))
				.filter(c -> total >= c.getMinSpending())
				.toList();
		dto.setUsableCoupons(usableCoupons);
		
		
		// 推薦優惠券 (可用優惠券中最大額；若有同樣面額，選快到期的)
		int max = 0;
		MyCouponDTO best = null;
		for (MyCouponDTO c : usableCoupons) {
			if (c.getAmount() <= totalAmount && c.getAmount() > max) {
				max = c.getAmount();
				best = c;
			} else if (c.getAmount() == max && best != null) {
				if (c.getIssueEndDate() != null && best.getIssueEndDate() != null && c.getIssueEndDate().isBefore(best.getIssueEndDate())) {
					best = c;
				}
			}
		}
		dto.setRecommendedCouponId(best != null ? best.getCouponId() : null);
		
		return dto;
	}
	

	
	// 結帳，新增訂單們
	@Transactional
	public void addOrder(ProductOrderRequestDTO orderReq, Integer userId) {

		// 取得完整收件地址
		CityDistrict district = cityDistrictRepo.findById(orderReq.getDistrictId())
				.orElseThrow(() -> new IllegalArgumentException("查無此區域"));
		String shippingAddress = district.getCityName() + district.getDistName() + orderReq.getDetailAddress();


		// 讀購物車商品
		List<ShoppingcartDTO> cartItems = loadCart(userId);

		
		// 建一個 Map 把購物車內有的商品 Id 對應它的商品詳情，之後可用 key(id) 帶出商品資料
		Map<Integer, ProductVO> prodMap = loadProducts(cartItems);

		
		// 新建一張會員訂單
		ProductOrderVO order = new ProductOrderVO();
		order.setUserId(userId);
		order.setShippingAddress(shippingAddress);
		order.setPaymentId(1); 		// 金流先給假值
		order.setCreatedAt(LocalDateTime.now());
		order.setOrderStatus(OrderStatus.pending);
		
		// 收集此訂單有那些小農
		Set<Integer> farmers = new HashSet<>();
		
		int totalAmount = 0;
		
		for (ShoppingcartDTO ci : cartItems) {
			ProductVO prod = prodMap.get(ci.getProductId());

			// 記錄此商品的明細
			ProductOrderItemVO orderItem = new ProductOrderItemVO();
			orderItem.setProductId(prod.getProductId());
			orderItem.setProductName(prod.getProductName());
			orderItem.setFarmerId(prod.getFarmerId());
			orderItem.setQuantity(ci.getQuantity());
			orderItem.setPrice(prod.getRetailPrice());
			orderItem.setShippedStatus(ShippedStatus.pending);
			orderItem.setPayoutStatus(PayoutStatus.pending);
			
			totalAmount += orderItem.getPrice() * orderItem.getQuantity();
			farmers.add(prod.getFarmerId()); 	// 記下小農 id
			
			// 關聯物件
			orderItem.setOrder(order);
			order.getItems().add(orderItem); 
		}
		order.setTotalAmount(totalAmount);
		
		// 套用優惠券，計算 discountAmount, finalPayment
		int discountAmount = 0;
		int finalPayment = totalAmount;
		
		String couponId = orderReq.getCoupon();
		if (couponId != null && !couponId.isBlank()) {
			discountAmount = couponSvc.useCoupon(userId, couponId, totalAmount);
			order.setCouponId(couponId);
			order.setDiscountAmount(discountAmount);
		}
		
		finalPayment = totalAmount - discountAmount;
		order.setFinalPayment(finalPayment);

		
		// 新增一筆訂單 ⇒ cascade all 同時新增 OrderItem
		ProductOrderVO o = orderRepo.save(order);
		
		// 發送通知 (小農/會員) sendProdOrderCreated(Set<Integer> farmerIds, Integer userId, Integer orderId)
		nSvc.sendProdOrderCreated(farmers, userId, o.getOrderId());
		System.out.println("訂單 " + o.getOrderId() +  " 建立成功!");
		
		// 清除購物車商品
		cartSvc.clearCart(userId);
	}
}
