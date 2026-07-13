package com.farmily.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import com.farmily.coupon.model.CouponDetailRepository;
import com.farmily.coupon.model.CouponDetailVO;
import com.farmily.coupon.model.CouponRepository;
import com.farmily.coupon.model.CouponVO;
import com.farmily.notification.service.NotificationService;
import com.farmily.product.dto.ProductOrderCheckoutInfoDTO;
import com.farmily.product.dto.ProductOrderFarmerResponseDTO;
import com.farmily.product.dto.ProductOrderItemFarmerResponseDTO;
import com.farmily.product.dto.ProductOrderItemResponseDTO;
import com.farmily.product.dto.ProductOrderRequestDTO;
import com.farmily.product.dto.ProductOrderResponseDTO;
import com.farmily.shoppingcart.dto.ShoppingcartDTO;
import com.farmily.shoppingcart.service.ProductShoppingCartService;
import com.farmily.user.dto.CityDistrictResponse;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.User;
import com.farmily.user.repository.CityDistrictRepository;

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
	private CouponRepository couponRepo;

	@Autowired
	private CouponDetailRepository couponDetailRepo;


	// 小農更新訂單出貨狀態
	@Transactional
	public void updateShippedStatus(Integer farmerId, Integer orderId) {
		ProductOrderVO order = orderRepo.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("查無此訂單"));
		
		// 檢查訂單是否屬於此小農
		if (!(order.getFarmerId().equals(farmerId))){
			throw new AccessDeniedException("無權限操作此訂單");
		}
 		
		// 檢查訂單是否尚未出貨
		if (order.getShippedStatus() != ShippedStatus.pending) {
			throw new IllegalStateException("訂單已出貨，無法再次操作");
		}
		
		order.setShippedStatus(ShippedStatus.shipping);
		order.setShippedAt(LocalDateTime.now());
		orderRepo.save(order);
	}
	
	// 會員確認收貨 (現階段設定: 消費者確認收貨同時更新 shipped_status, received_at, order_status, payout_status, completed_at)
	@Transactional
	public void updateReceived(Integer userId, Integer orderId) {
		ProductOrderVO order = orderRepo.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("查無此訂單"));
		
		// 檢查訂單是否屬於此會員
		if (!(order.getUserId().equals(userId))) {
			throw new AccessDeniedException("無權限操作此訂單");
		}
		
		// 檢查訂單是否已出貨
		if (order.getShippedStatus() == ShippedStatus.pending) {
			throw new IllegalStateException("尚未出貨，無法確認收貨");
		}
		if (order.getShippedStatus() == ShippedStatus.delivered) {
			throw new IllegalStateException("訂單已確認貨，無法再次操作");
		}
			
		order.setShippedStatus(ShippedStatus.delivered);
		order.setReceivedAt(LocalDateTime.now());
		order.setOrderStatus(OrderStatus.confirmed);
		order.setPayoutStatus(PayoutStatus.paid);
		order.setCompletedAt(LocalDateTime.now());
		orderRepo.save(order);				
	}

	// 取得會員訂單列表
	@Transactional (readOnly = true)
	public Page<ProductOrderResponseDTO> getOrderByUser(Integer userId, int page){
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
		Page<ProductOrderVO> list = orderRepo.findByUserId(userId, pageable);
		Page<ProductOrderResponseDTO> dtoList = list.map(this::toOrderDTO);
		return dtoList;
	}


	// 取得會員訂單明細
	@Transactional (readOnly = true)
	public Page<ProductOrderItemResponseDTO> getOrderItems(Integer userId, Integer orderId, int page){
		ProductOrderVO order = orderRepo.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("查無此訂單"));
		
		// 檢查訂單是否屬於此會員
		if (!(order.getUserId().equals(userId))) {
			throw new AccessDeniedException("無權限查看此訂單");
		}
		
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("orderItemId").ascending());
		Page<ProductOrderItemVO> list = orderItemRepo.findByOrder_OrderId(orderId, pageable);
		Page<ProductOrderItemResponseDTO> dtoList = list.map(this::toOrderItemDTO);
		return dtoList;
	}
<<<<<<< Upstream, based on branch 'master' of https://github.com/ericlee-775/CKA101G3.git

=======
	
	
	// 取得小農訂單列表
	public Page<ProductOrderFarmerResponseDTO> getOrderByFarmer(Integer farmerId, int page){
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
		Page<ProductOrderVO> list = orderRepo.findByFarmerId(farmerId, pageable);
		Page<ProductOrderFarmerResponseDTO> dtoList = list.map(this::toFarmerOrderDTO);
		
		return dtoList;
	}
	
	// 取得小農訂單明細
	public Page<ProductOrderItemFarmerResponseDTO> getOrderItemsFarmer(Integer farmerId, Integer orderId, int page){
		ProductOrderVO order = orderRepo.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("查無此訂單"));
		if (!(order.getFarmerId().equals(farmerId))) {
			throw new AccessDeniedException("無權限查看此訂單");
		}
		
		Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("orderItemId").descending());
		Page<ProductOrderItemVO> list = orderItemRepo.findByOrder_OrderId(orderId, pageable);
		Page<ProductOrderItemFarmerResponseDTO> dtoList = list.map(this::toFarmerOrderItemDTO);
		
		return dtoList;
	}
	
	
>>>>>>> f3b7900 新增 商品訂單 service, controller (farmer)
	// 把 Repository 查回來的 orderVO 轉成 orderDTO
	private ProductOrderResponseDTO toOrderDTO(ProductOrderVO vo){
		ProductOrderResponseDTO dto = new ProductOrderResponseDTO();
		dto.setCreatedAt(vo.getCreatedAt());
		dto.setOrderId(vo.getOrderId());
		dto.setDiscountAmount(vo.getDiscountAmount());
		dto.setFinalPayment(vo.getFinalPayment());
		dto.setShippedAt(vo.getShippedAt());
		dto.setReceivedAt(vo.getReceivedAt());
		dto.setShippedStatus(vo.getShippedStatus().name());
		dto.setOrderStatus(vo.getOrderStatus().name());

		return dto;
	}

	// 把 Repository 查回來的 itemVO 轉成 itemDTO
	private ProductOrderItemResponseDTO toOrderItemDTO(ProductOrderItemVO vo){
		ProductOrderItemResponseDTO dto = new ProductOrderItemResponseDTO();
		dto.setProductName(vo.getProductName());
		dto.setProductId(vo.getProductId());
		dto.setFarmerId(vo.getOrder().getFarmerId());
		dto.setPrice(vo.getPrice());
		dto.setQuantity(vo.getQuantity());

		return dto;
	}
<<<<<<< Upstream, based on branch 'master' of https://github.com/ericlee-775/CKA101G3.git



=======
		
	// 把 Repository 查回來的 orderVO 轉成小農 orderDTO
	private ProductOrderFarmerResponseDTO toFarmerOrderDTO(ProductOrderVO vo) {
		ProductOrderFarmerResponseDTO dto = new ProductOrderFarmerResponseDTO();
		dto.setOrderId(vo.getOrderId());
		dto.setUserId(vo.getUserId());
		dto.setShippingAddress(vo.getShippingAddress());
		dto.setCreatedAt(vo.getCreatedAt());
		dto.setTotalAmount(vo.getTotalAmount());
		dto.setShippedStatus(vo.getShippedStatus().name());
		dto.setReceivedAt(vo.getReceivedAt());
		dto.setPayoutStatus(vo.getPayoutStatus().name());
		dto.setCompletedAt(vo.getCompletedAt());
		
		return dto;
	}
	
	// 把 Repository 查回來的 itemVO 轉成小農 itemDTO 
	private ProductOrderItemFarmerResponseDTO toFarmerOrderItemDTO(ProductOrderItemVO vo) {
		ProductOrderItemFarmerResponseDTO dto = new ProductOrderItemFarmerResponseDTO();
		dto.setProductName(vo.getProductName());
		dto.setProductId(vo.getProductId());
		dto.setPrice(vo.getPrice());
		dto.setQuantity(vo.getQuantity());
		
		return dto;
	}
	
		
>>>>>>> f3b7900 新增 商品訂單 service, controller (farmer)
	// 取得預設配送地址
	public ProductOrderCheckoutInfoDTO getCheckoutInfo(User u) {
		ProductOrderCheckoutInfoDTO dto = new ProductOrderCheckoutInfoDTO();

		dto.setDistrict(CityDistrictResponse.from(u.getCityDistrict()));
		dto.setDetailAddress(u.getUserAddress());

		return dto;
	}


	// 結帳，新增訂單們
	@Transactional
	public void addOrder(ProductOrderRequestDTO orderReq, Integer userId) {

		// 取得完整收件地址
		CityDistrict district = cityDistrictRepo.findById(orderReq.getDistrictId())
				.orElseThrow(() -> new IllegalArgumentException("查無此區域"));
		String shippingAddress = district.getCityName() + district.getDistName() + orderReq.getDetailAddress();

		// 檢查優惠券有沒有重複使用 (多筆訂單填了相同的券碼)
		// 從 Map 中取出有填的優惠券碼 (排除 null)
		List<String> usedCoupon = orderReq.getCoupon().values().stream()
				.filter(c -> c != null)
				.toList();

		// 用 Set 去除 List 中重複的碼
		Set<String> uniqueCoupon = new HashSet<>(usedCoupon);

		// 變少表示有重複填入
		if (usedCoupon.size() > uniqueCoupon.size()) {
			throw new IllegalStateException("同一優惠碼不能重複使用");
		}


		// 讀購物車商品
		List<ShoppingcartDTO> cartItems = cartSvc.getcart(userId);
		if (cartItems == null || cartItems.isEmpty()) {
			throw new IllegalStateException("購物車為空");
		}

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

		/*
		// 使用 stream() 版本
		Map<Integer, ProductVO> prodMap = prods.stream()
				.collect(Collectors.toMap(ProductVO::getProductId, prod -> prod));
		*/

		// 檢查商品上架狀態 (比對購物車的 productId 和建好的商品 VO Map)，並重查一次當下商品價格
		Map<Integer, List<ProductOrderItemVO>> byFarmer = new HashMap<>();

		for (ShoppingcartDTO ci : cartItems) {
			ProductVO prod = prodMap.get(ci.getProductId());
			if (prod == null) {
				throw new IllegalStateException("商品不存在" + ci.getProductId());
			}
			if (prod.getStatus() != ProductStatus.ACTIVE) {
				throw new IllegalStateException("商品已下架" + ci.getProductId());
			}

			Integer retailPrice = prod.getRetailPrice();
			Integer farmerId = prod.getFarmerId();
			String prodName = prod.getProductName();

			// 記錄此商品的明細
			ProductOrderItemVO orderItem = new ProductOrderItemVO();
			orderItem.setProductId(prod.getProductId());
			orderItem.setProductName(prodName);
			orderItem.setQuantity(ci.getQuantity());
			orderItem.setPrice(retailPrice);

			// 依 farmerId 拆單 (Map<farmerId, items>)
			if (byFarmer.containsKey(farmerId)) {
				byFarmer.get(farmerId).add(orderItem);
			}
			else {
				List<ProductOrderItemVO> list = new ArrayList<>();
				list.add(orderItem);
				byFarmer.put(farmerId, list);
			}
		}

		// 按照小農新增訂單 (一個小農一筆訂單)
		for (Map.Entry<Integer, List<ProductOrderItemVO>> e : byFarmer.entrySet()) {
			ProductOrderVO order = new ProductOrderVO();
			Integer farmerId = e.getKey();
			order.setUserId(userId);
			order.setFarmerId(farmerId);
			order.setShippingAddress(shippingAddress);
			order.setPaymentId(0); 		// 金流先給假值
			order.setCreatedAt(LocalDateTime.now());
			order.setShippedStatus(ShippedStatus.pending);
			order.setOrderStatus(OrderStatus.pending);
			order.setPayoutStatus(PayoutStatus.pending);

			// 計算金額
			Integer totalAmount = 0;
			for (ProductOrderItemVO item : e.getValue()) {
				totalAmount += item.getPrice() * item.getQuantity();

				// 關聯物件
				item.setOrder(order);
				order.getItems().add(item);
			}

			order.setTotalAmount(totalAmount);

			Integer discountAmount = 0;
			Integer finalPayment = totalAmount;
/*
			// 優惠券
			// 目前設定購物車拆單後，各小農訂單各自選優惠券
			// 檢查此小農訂單是否有用優惠券
			if (orderReq.getCoupon().containsKey(farmerId)) {
				String couponId = orderReq.getCoupon().get(farmerId);
				if (couponId != null) {

					// 檢查有沒有這個優惠券代碼
					CouponVO coupon = couponRepo.findById(couponId).orElseThrow(() -> new IllegalStateException("優惠券不存在"));

					// 檢查使用者有沒有該優惠券，驗證優惠券狀態(未使用)
					CouponDetailVO userCoupon = couponDetailRepo.findByUserIdAndCouponId(userId, couponId);
					if (userCoupon != null && userCoupon.getStatus() == com.farmily.coupon.model.Status.ACTIVE) {

							// 檢查優惠卷金額是否達到門檻
							if (!(totalAmount >= coupon.getMinSpending())) {
								throw new IllegalStateException("優惠券未達使用門檻");
							}
							discountAmount = coupon.getAmount();

						// 套用優惠券，計算 finalPayment (不能變負數)
						finalPayment = Math.max(0, totalAmount - discountAmount);
						order.setCouponId(couponId);

						// 把使用的優惠券標記為已使用
//						couponSvc.updateStatus(userId,couponId);
					}
					else {
						throw new IllegalStateException("優惠券無法使用");
					}
				}
			}
*/
			order.setDiscountAmount(discountAmount);
			order.setFinalPayment(finalPayment);


			// 新增一筆訂單 ⇒ cascade all 同時新增 OrderItem
			ProductOrderVO o = orderRepo.save(order);
			
			// 發送通知 (小農/會員)
			nSvc.sendProdOrderCreated(farmerId, userId, o.getOrderId());
			System.out.println("訂單 " + o.getOrderId() +  " 建立成功!");
		}


		// 清除購物車商品
		cartSvc.clearCart(userId);
<<<<<<< Upstream, based on branch 'master' of https://github.com/ericlee-775/CKA101G3.git

		// 發送通知 (小農/會員)
		System.out.println("訂單建立成功!");
=======
		
>>>>>>> 8ccfc52 新增 訂單成立發送通知方法

	}
<<<<<<< Upstream, based on branch 'master' of https://github.com/ericlee-775/CKA101G3.git


=======
	
>>>>>>> 8ccfc52 新增 訂單成立發送通知方法
}
