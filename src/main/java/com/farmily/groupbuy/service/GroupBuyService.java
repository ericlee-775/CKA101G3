package com.farmily.groupbuy.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.groupbuy.model.GroupBuyDetailDTO;
import com.farmily.groupbuy.model.GroupBuyFarmerDTO;
import com.farmily.groupbuy.model.GroupBuyHostCreateDTO;
import com.farmily.groupbuy.model.GroupBuyOrderDTO;
import com.farmily.groupbuy.model.GroupBuyOrderRepository;
import com.farmily.groupbuy.model.GroupBuyOrderVO;
import com.farmily.groupbuy.model.GroupBuyParticipationDTO;
import com.farmily.groupbuy.model.GroupBuyParticipationRepository;
import com.farmily.groupbuy.model.GroupBuyParticipationVO;
import com.farmily.groupbuy.model.GroupBuyRepository;
import com.farmily.groupbuy.model.GroupBuyShowToUserJoinDTO;
import com.farmily.groupbuy.model.GroupBuyStatus;
import com.farmily.groupbuy.model.GroupBuyVO;
import com.farmily.groupbuy.model.GroupBuyWishListId;
import com.farmily.groupbuy.model.GroupBuyWishListRepository;
import com.farmily.groupbuy.model.GroupBuyWishListVO;
import com.farmily.groupbuy.model.JoinStatus;
import com.farmily.groupbuy.model.OrderStatus;
import com.farmily.groupbuy.model.PaidStatus;
import com.farmily.groupbuy.model.RequestStatus;
import com.farmily.groupbuy.model.ShippedStatus;
import com.farmily.groupbuy.model.ShowJoinedGroupBuyDTO;
import com.farmily.groupbuy.model.UnderReviewDTO;
import com.farmily.groupbuy.model.WishListDTO;
import com.farmily.product.dto.ProductGroupBuyDTO;
import com.farmily.product.model.ProductVO;
import com.farmily.product.service.ProductServiceImpl;
import com.farmily.user.model.User;
import com.farmily.user.repository.UserRepository;

@Service
public class GroupBuyService {
	@Autowired
	GroupBuyRepository repository;

	@Autowired
	GroupBuyWishListRepository wishListRepository;

	@Autowired
	ProductServiceImpl productSvc;

	@Autowired
	UserRepository userRepository;

	@Autowired
	GroupBuyOrderRepository groupBuyOrderRepository;

	@Autowired
	GroupBuyParticipationRepository participationRepository;

	// 會員加入單筆團購收藏
	@Transactional
	public void groupBuyFavorite(Integer userId, Integer groupBuyId) {
		GroupBuyVO groupBuy = repository.findById(groupBuyId).orElseThrow(() -> new RuntimeException("查無此團購"));
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("查無此會員"));

		if (groupBuy.getStatus() != GroupBuyStatus.open) {
			throw new RuntimeException("只有開團中的團購可以收藏");
		}

		GroupBuyWishListId id = new GroupBuyWishListId(userId, groupBuyId);

		if (wishListRepository.existsById(id)) {
			throw new RuntimeException("此團購已收藏過");
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		GroupBuyWishListVO wish = new GroupBuyWishListVO();
		wish.setId(id);
		wish.setUser(user);
		wish.setGroupBuy(groupBuy);
		wish.setSavedDatetime(now);
		wishListRepository.save(wish);
	}

	// 會員移除單筆團購收藏
	@Transactional
	public void removeGroupBuyFavorite(Integer userId, Integer groupBuyId) {
		GroupBuyWishListId id = new GroupBuyWishListId(userId, groupBuyId);
		if (!wishListRepository.existsByUser_UserIdAndGroupBuy_GroupBuyId(userId, groupBuyId)) {
			throw new RuntimeException("未收藏此團購");
		}
		wishListRepository.deleteById(id);
	}

	// 加入團購
	@Transactional
	public void joinGroupBuy(GroupBuyShowToUserJoinDTO form, Integer groupBuyId, Integer userId) {
		GroupBuyVO groupBuyVO = repository.findById(groupBuyId).orElseThrow(() -> new RuntimeException("查無此團購"));
		GroupBuyParticipationVO groupBuyParticipationVO = new GroupBuyParticipationVO();
		if (form.getBuyQty() == null || form.getBuyQty() <= 0) {
			throw new RuntimeException("請輸入正確的購買數量");
		}
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("查無此會員"));

		if (participationRepository.existsByGroupBuyIdAndUserId(groupBuyVO, user)) {
			throw new RuntimeException("您已參加過此團購");
		}
		boolean isHost = groupBuyVO.getHostUser().getUserId().equals(userId);// 從團購主表當中判斷是否為團購主
		Integer paidAmount = (form.getBuyQty() * groupBuyVO.getGroupPrice());
		Timestamp now = new Timestamp(System.currentTimeMillis());
		groupBuyParticipationVO.setBuyQty(form.getBuyQty());
		groupBuyParticipationVO.setUserId(user);
		groupBuyParticipationVO.setGroupBuyId(groupBuyVO);
		groupBuyParticipationVO.setJoinDatetime(now);
		groupBuyParticipationVO.setJoinStatus(JoinStatus.active);
		groupBuyParticipationVO.setPaidAmount(paidAmount);
		groupBuyParticipationVO.setPaidDatetime(now);
		groupBuyParticipationVO.setHost(isHost);

		participationRepository.save(groupBuyParticipationVO);
	}

	// 團購主發起請求
	@Transactional
	public void hostRequest(GroupBuyHostCreateDTO form, Integer productId, Integer hostUserId) {
		ProductVO product = productSvc.getGroupBuyProductById(productId);
		if (product == null) {
			throw new RuntimeException("查無此商品");
		}
		Integer minimumTargetAmount=product.getGroupPrice()*2;
		User hostUser = userRepository.findById(hostUserId).orElseThrow(() -> new RuntimeException("查無此會員"));
		GroupBuyVO groupBuyVO = new GroupBuyVO();
		groupBuyVO.setHostUser(hostUser);
		groupBuyVO.setProduct(product);
		if(form.getTargetAmount()<minimumTargetAmount){
			throw new RuntimeException("達標金額不得低於"+minimumTargetAmount+"元");
		}else {
		groupBuyVO.setTargetAmount(form.getTargetAmount());
		groupBuyVO.setOpenDatetime(Timestamp.valueOf(form.getOpenDatetime().atStartOfDay()));
		groupBuyVO.setDdlDatetime(Timestamp.valueOf(form.getDdlDatetime().atTime(23, 59, 59)));
		groupBuyVO.setPickupAddress(form.getPickupAddress());
		// 系統預設
		groupBuyVO.setRequestStatus(RequestStatus.pending);
		groupBuyVO.setStatus(GroupBuyStatus.pending);
		groupBuyVO.setRequestDatetime(new Timestamp(System.currentTimeMillis()));
		groupBuyVO.setGroupPrice(product.getGroupPrice());

		repository.save(groupBuyVO);
		}
	}

	// 給會員看已成立之團購訂單
	public List<GroupBuyOrderDTO> showMySuccessGroupBuyOrders(Integer userId) {
		List<GroupBuyOrderVO> orders = groupBuyOrderRepository.findMySuccessOrders(userId, JoinStatus.active,
				GroupBuyStatus.success);

		return orders.stream().map(vo -> {

			GroupBuyParticipationVO participation = participationRepository
					.findByGroupBuyId_GroupBuyIdAndUserId_UserId(vo.getGroupBuyId().getGroupBuyId(), userId)
					.orElseThrow(() -> new RuntimeException("查無此會員的團購參與紀錄"));
			GroupBuyOrderDTO dto = new GroupBuyOrderDTO();
			dto.setBuyQty(participation.getBuyQty());
			dto.setPaidAmount(participation.getPaidAmount());
			dto.setOrderId(vo.getOrderId());
			dto.setGroupBuyId(vo.getGroupBuyId().getGroupBuyId());
			dto.setGroupPrice(vo.getGroupPrice());
			dto.setShippingAddress(vo.getGroupBuyId().getPickupAddress());
			dto.setShippedStatus(vo.getShippedStatus());
			dto.setShippedAt(vo.getShippedAt());
			dto.setCreatedAt(vo.getCreatedAt());
			dto.setReceivedAt(vo.getReceivedAt());
			dto.setOrderStatus(vo.getOrderStatus());
			dto.setPaidStatus(vo.getPaidStatus());
			dto.setCompletedAt(vo.getCompletedAt());
			dto.setHostUserEmail(vo.getGroupBuyId().getHostUser().getEmail());
			dto.setHostUserName(vo.getGroupBuyId().getHostUser().getUserName());
			dto.setHostUserPhone(vo.getGroupBuyId().getHostUser().getUserPhoneNum());
			dto.setProductId(vo.getGroupBuyId().getProduct().getProductId());
			return dto;
		}).toList();
	}

	// 小農訂單出貨狀態更改為已送達
	@Transactional
	public void ship(Integer orderId, Integer farmerId) {
		GroupBuyOrderVO order = groupBuyOrderRepository.findByOrderIdAndGroupBuyId_Product_FarmerId(orderId, farmerId)
				.orElseThrow(() -> new RuntimeException("查無此訂單或你沒有權限"));
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (order.getShippedStatus() == ShippedStatus.DELIVERED) {
			throw new RuntimeException("請勿重複更改出貨狀態");
		}
		order.setShippedStatus(ShippedStatus.DELIVERED);
		order.setShippedAt(now);
		order.setOrderStatus(OrderStatus.PENDING);

		groupBuyOrderRepository.save(order);
	}

	// 團購主確認收貨用
	@Transactional
	public void confirmReceipt(Integer orderId, Integer userId) {
		GroupBuyOrderVO orderVO = groupBuyOrderRepository.findByOrderIdAndGroupBuyId_HostUser_UserId(orderId, userId)
				.orElseThrow(() -> new RuntimeException("查無此訂單，或你不是此團購的團購主"));
		if (orderVO.getOrderStatus() == OrderStatus.COMPLETED) {
			throw new RuntimeException("此訂單已經確認收貨");
		}

		if (orderVO.getShippedStatus() == ShippedStatus.PENDING) {
			throw new RuntimeException("商品尚未出貨，無法確認收貨");
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());

		orderVO.setOrderStatus(OrderStatus.COMPLETED);
		orderVO.setPaidStatus(PaidStatus.PAID);
		orderVO.setReceivedAt(now);
		orderVO.setCompletedAt(now);

		groupBuyOrderRepository.save(orderVO);
	}
	

	//給小農看團購總收益
	@Transactional(readOnly=true)
	public Long getGroupBuyTotalRevenue(Integer farmerId) {
		return groupBuyOrderRepository.sumTotalAmountByPaidStatusAndFarmerId(PaidStatus.PAID,farmerId);
	}
	

	// 給小農審核團購申請用
	@Transactional
	public void reviewGroupBuy(Integer groupBuyId, Integer farmerId, RequestStatus requestStatus, String rejectReason) {
		GroupBuyVO groupBuyVO = repository.findByGroupBuyIdAndProduct_FarmerId(groupBuyId, farmerId)
				.orElseThrow(() -> new RuntimeException("查無此團購申請或你沒有權限審核"));

		if (requestStatus == null) {
			throw new RuntimeException("請選擇審核結果");
		}

		if (groupBuyVO.getRequestStatus() == RequestStatus.approved
				|| groupBuyVO.getRequestStatus() == RequestStatus.rejected) {
			throw new RuntimeException("此團購申請已審核過，請勿重複審核");
		}

		if (requestStatus == RequestStatus.rejected && (rejectReason == null || rejectReason.trim().isEmpty())) {
			throw new RuntimeException("拒絕時請填寫拒絕原因");
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		groupBuyVO.setReplyDatetime(now);
		groupBuyVO.setRequestStatus(requestStatus);

		if (requestStatus == RequestStatus.approved) {
			groupBuyVO.setCreatedAt(now);
			groupBuyVO.setRejectReason(null);
			groupBuyVO.setStatus(GroupBuyStatus.open);
		} else if (requestStatus == RequestStatus.rejected) {
			groupBuyVO.setRejectReason(rejectReason);
			groupBuyVO.setStatus(GroupBuyStatus.cancelled);
		}

		repository.save(groupBuyVO);
	}

	// 給一般消費者抓的單筆團購資料(公開頁面無須登入)
	public GroupBuyDetailDTO getOneGroupBuyDetail(Integer groupBuyId) {
		GroupBuyVO gb = repository.findById(groupBuyId).orElseThrow(() -> new RuntimeException("查無此團購"));
		return new GroupBuyDetailDTO(gb.getProduct().getProductId(), gb.getGroupBuyId(),
				gb.getProduct().getProductName(), gb.getGroupPrice(), gb.getTargetAmount(), gb.getOpenDatetime(),
				gb.getDdlDatetime(), gb.getPickupAddress(), gb.getStatus(), gb.getProduct().getRetailPrice());
	}

	// 給團購主看的團購訂單
	@Transactional(readOnly = true)
	public List<UnderReviewDTO> showMyGroupBuyRequests(Integer userId) {

		List<GroupBuyVO> groupBuyList = repository.findByHostUser_UserId(userId);

		return groupBuyList.stream()
				.map(vo -> new UnderReviewDTO(vo.getGroupBuyId(), vo.getProduct().getProductName(), vo.getGroupPrice(),
						vo.getRequestStatus(), vo.getTargetAmount(), vo.getOpenDatetime(), vo.getDdlDatetime(),
						vo.getRejectReason(), vo.getReplyDatetime(),vo.getRequestDatetime()))
				.toList();
	}

	// 給一般會員看已加入但還沒成團的團購
	@Transactional(readOnly = true)
	public List<ShowJoinedGroupBuyDTO> showJoinedGroupBuy(Integer userId) {

		List<GroupBuyParticipationVO> list = participationRepository.findByUserId_UserId(userId);

		return list.stream().map(joined -> {

			GroupBuyVO groupBuy = joined.getGroupBuyId();

			Integer targetAmount = groupBuy.getTargetAmount();// 目標金額
			// currentPaidAmount=目前所有active參與者已付款金額
			Integer currentPaidAmount = participationRepository.sumPaidAmountByGroupBuy(groupBuy, JoinStatus.active);
			// difference=還差多少錢成團
			Integer difference = targetAmount - currentPaidAmount;
			// 如果已經達標，不要顯示負數
			if (difference < 0) {
				difference = 0;
			}
			return new ShowJoinedGroupBuyDTO(groupBuy.getStatus(), groupBuy.getDdlDatetime(),
					groupBuy.getPickupAddress(), groupBuy.getProduct().getProductName(), joined.getBuyQty(),
					joined.getPaidAmount(), targetAmount, difference,
					joined.getGroupBuyId().getProduct().getProductId());
		}).toList();
	}

	// 給會員看的團購收藏
	public List<WishListDTO> showGroupBuyFavoriteList(Integer userId) {

		List<GroupBuyWishListVO> list = wishListRepository
				.findByUser_UserIdAndGroupBuy_StatusOrderBySavedDatetimeDesc(userId, GroupBuyStatus.open);
		return list.stream().map(f -> {
			GroupBuyVO groupBuy = f.getGroupBuy();
			Integer targetAmount = groupBuy.getTargetAmount();
			Integer currentPaidAmount = participationRepository.sumPaidAmountByGroupBuy(groupBuy, JoinStatus.active);
			if (currentPaidAmount == null) {
				currentPaidAmount = 0;
			}
			Integer difference = Math.max(targetAmount - currentPaidAmount, 0);

			return new WishListDTO(f.getGroupBuy().getHostUser().getUserName(), f.getGroupBuy().getGroupBuyId(),
					f.getGroupBuy().getProduct().getRetailPrice(), f.getGroupBuy().getStatus(),
					f.getGroupBuy().getDdlDatetime(), f.getGroupBuy().getPickupAddress(),
					f.getGroupBuy().getProduct().getProductName(), f.getGroupBuy().getGroupPrice(),
					f.getGroupBuy().getProduct().getProductId(), f.getGroupBuy().getTargetAmount(), currentPaidAmount,
					difference, true

			);

		}).toList();
	}

	// 小農前台看訂單總表
	@Transactional
	public List<GroupBuyOrderDTO> showOrderList(Integer farmerId) {
		List<GroupBuyOrderVO> list = groupBuyOrderRepository.findByGroupBuyId_Product_FarmerId(farmerId);
		return list.stream().map(order -> {
			GroupBuyOrderDTO dto = new GroupBuyOrderDTO();
			dto.setOrderId(order.getOrderId());
			dto.setGroupBuyId(order.getGroupBuyId().getGroupBuyId());
			dto.setTotalQuantity(order.getTotalQuantity());
			dto.setGroupPrice(order.getGroupPrice());
			dto.setTotalAmount(order.getTotalAmount());
			dto.setShippingAddress(order.getGroupBuyId().getPickupAddress());
			dto.setShippedStatus(order.getShippedStatus());
			dto.setCreatedAt(order.getCreatedAt());
			return dto;

		}).toList();

	}

	// 給小農前台查看進行中團購清單用
	@Transactional(readOnly = true)
	public List<GroupBuyFarmerDTO> showGroupBuyList(Integer farmerId) {
		List<GroupBuyVO> list = repository.findByProduct_FarmerId(farmerId);
		return list.stream()
				.map(gb -> new GroupBuyFarmerDTO(gb.getGroupBuyId(), gb.getProduct().getProductId(),
						gb.getProduct().getProductName(), gb.getGroupPrice(),
						gb.getHostUser() != null ? gb.getHostUser().getUserName() : null, gb.getTargetAmount(),
						gb.getOpenDatetime(), gb.getDdlDatetime(), gb.getPickupAddress(), gb.getRequestStatus(),
						gb.getRequestDatetime(), gb.getStatus(), gb.getReplyDatetime(), gb.getRejectReason()))
				.toList();
	}

	// 給管理員的總表
	@Transactional(readOnly = true)
	public List<GroupBuyVO> showAllGroupBuyToAdmin() {
		return repository.findAll();
	}

	// 給管理員的訂單表
	@Transactional
	public List<GroupBuyOrderVO> showAllGroupBuyOrderToAdmin() {
		return groupBuyOrderRepository.findAll();
	}

	// 查看單筆團購訂單(給小農與管理員)
	public GroupBuyOrderDTO showOneOrder(Integer orderId) {
		GroupBuyOrderVO vo = groupBuyOrderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("查無此團購訂單"));
		GroupBuyOrderDTO dto = new GroupBuyOrderDTO();
		dto.setOrderId(vo.getOrderId());
		dto.setGroupBuyId(vo.getGroupBuyId().getGroupBuyId());
		dto.setTotalQuantity(vo.getTotalQuantity());
		dto.setGroupPrice(vo.getGroupPrice());
		dto.setTotalAmount(vo.getTotalAmount());
		dto.setShippingAddress(vo.getGroupBuyId().getPickupAddress());
		dto.setShippedStatus(vo.getShippedStatus());
		dto.setShippedAt(vo.getShippedAt());
		dto.setCreatedAt(vo.getCreatedAt());
		dto.setReceivedAt(vo.getReceivedAt());
		dto.setOrderStatus(vo.getOrderStatus());
		dto.setPaidStatus(vo.getPaidStatus());
		dto.setCompletedAt(vo.getCompletedAt());
		dto.setHostUserName(vo.getGroupBuyId().getHostUser().getUserName());
		dto.setHostUserEmail(vo.getGroupBuyId().getHostUser().getEmail());
		dto.setHostUserPhone(vo.getGroupBuyId().getHostUser().getUserPhoneNum());
		return dto;
	}

	public List<GroupBuyVO> getOpenGroupBuys() {
		return repository.findByRequestStatusAndStatus(RequestStatus.approved, GroupBuyStatus.open);
	}

	// 給小農看的單一團購資料
	@Transactional(readOnly = true)
	public GroupBuyFarmerDTO getOneGroupBuyForFarmer(Integer groupBuyId, Integer farmerId) {
		GroupBuyVO gb = repository.findByGroupBuyIdAndProduct_FarmerId(groupBuyId, farmerId)
				.orElseThrow(() -> new RuntimeException("查無此團購或你沒有權限"));

		return new GroupBuyFarmerDTO(gb.getGroupBuyId(), gb.getProduct().getProductId(),
				gb.getProduct().getProductName(), gb.getGroupPrice(),
				gb.getHostUser() != null ? gb.getHostUser().getUserName() : null, gb.getTargetAmount(),
				gb.getOpenDatetime(), gb.getDdlDatetime(), gb.getPickupAddress(), gb.getRequestStatus(),
				gb.getRequestDatetime(), gb.getStatus(), gb.getReplyDatetime(), gb.getRejectReason());
	}

	public GroupBuyParticipationVO getGroupBuyId(Integer groupBuyId) {
		return participationRepository.findById(groupBuyId).orElse(null);
	}

	// 給小農看的開團中團購摘要：團購主、取貨地址、目前總數量與總金額
	@Transactional(readOnly = true)
	public GroupBuyParticipationDTO showGroupBuyProgress(Integer groupBuyId, Integer farmerId) {
		GroupBuyVO groupBuy = repository.findByGroupBuyIdAndProduct_FarmerId(groupBuyId, farmerId)
				.orElseThrow(() -> new RuntimeException("查無此團購或你沒有權限"));
		Integer totalAmount = participationRepository.sumPaidAmountByGroupBuy(groupBuy, JoinStatus.active);
		Integer totalQuantity = participationRepository.sumtotalQuantityByGroupBuy(groupBuy, JoinStatus.active);
		if (totalAmount == null) {
			totalAmount = 0;
		}
		if (totalQuantity == null) {
			totalQuantity = 0;
		}
		GroupBuyParticipationDTO dto = new GroupBuyParticipationDTO();
		dto.setGroupBuyId(groupBuy.getGroupBuyId());
		dto.setHostName(groupBuy.getHostUser().getUserName());
		dto.setPickupAddress(groupBuy.getPickupAddress());
		dto.setTotalAmount(totalAmount);
		dto.setTotalQuantity(totalQuantity);
		return dto;

	}

	// 排程判別成團與否，若成團即將此筆團購轉為訂單，預設為當天00:00檢查前一天的團購是否有截止

	// 共用團購結算邏輯
	private void settleGroupBuy(GroupBuyVO groupBuy) {
		Integer totalQuantity = participationRepository.sumtotalQuantityByGroupBuy(groupBuy, JoinStatus.active);

		Integer totalAmount = participationRepository.sumPaidAmountByGroupBuy(groupBuy, JoinStatus.active);

		if (totalQuantity == null) {
			totalQuantity = 0;
		}

		if (totalAmount == null) {
			totalAmount = 0;
		}

		if (totalAmount >= groupBuy.getTargetAmount()) {
			groupBuy.setStatus(GroupBuyStatus.success);

			GroupBuyOrderVO order = new GroupBuyOrderVO();

			order.setGroupBuyId(groupBuy);
			order.setGroupPrice(groupBuy.getGroupPrice());
			order.setTotalAmount(totalAmount);
			order.setTotalQuantity(totalQuantity);
			order.setShippedStatus(ShippedStatus.PENDING);
			order.setShippedAt(null);
			order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
			order.setReceivedAt(null);
			order.setOrderStatus(OrderStatus.PENDING);
			order.setPaidStatus(PaidStatus.UNPAID);
			order.setCompletedAt(null);

			groupBuyOrderRepository.save(order);

		} else {
			groupBuy.setStatus(GroupBuyStatus.failed);
		}

		repository.save(groupBuy);
	}

	// 自動
	@Transactional
	public void checkExpiredGroupBuys() {
		Timestamp now = new Timestamp(System.currentTimeMillis());

		List<GroupBuyVO> expiredList = repository.findByStatusAndDdlDatetimeBefore(GroupBuyStatus.open, now);

		for (GroupBuyVO groupBuy : expiredList) {
			settleGroupBuy(groupBuy);
		}
	}

	// 展示手動結算用
	@Transactional
	public void forceSettleGroupBuy(Integer groupBuyId) {
		GroupBuyVO groupBuy = repository.findById(groupBuyId).orElseThrow(() -> new RuntimeException("查無此團購"));

		if (groupBuy.getStatus() != GroupBuyStatus.open) {
			throw new RuntimeException("此團購目前不是開團中");
		}

		settleGroupBuy(groupBuy);
	}

	// 公開前台顯示可團購之商品(前端可用篩選方式分成可加入、可發起)
	public List<ProductGroupBuyDTO> getConsumerGroupBuyList(List<GroupBuyStatus> statuses) {
		List<GroupBuyVO> groupBuyList = repository.findByRequestStatusAndStatusIn(RequestStatus.approved, statuses);
		return groupBuyList.stream()
				.map(groupBuy -> new ProductGroupBuyDTO(groupBuy.getGroupBuyId(), groupBuy.getProduct().getProductId(),
						groupBuy.getProduct().getProductName(), groupBuy.getGroupPrice(), groupBuy.getTargetAmount(),
						groupBuy.getOpenDatetime(), groupBuy.getDdlDatetime(), groupBuy.getPickupAddress(),
						groupBuy.getStatus(),groupBuy.getProduct().getFarmer().getFarmName()))
				.toList();
	}

}