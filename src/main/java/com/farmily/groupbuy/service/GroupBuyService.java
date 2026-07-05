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
import com.farmily.groupbuy.model.GroupBuyParticipationRepository;
import com.farmily.groupbuy.model.GroupBuyParticipationVO;
import com.farmily.groupbuy.model.GroupBuyRepository;
import com.farmily.groupbuy.model.GroupBuyShowToUserJoinDTO;
import com.farmily.groupbuy.model.GroupBuyStatus;
import com.farmily.groupbuy.model.GroupBuyVO;
import com.farmily.groupbuy.model.JoinStatus;
import com.farmily.groupbuy.model.RequestStatus;
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
	ProductServiceImpl productSvc;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	GroupBuyOrderRepository groupBuyOrderRepository;

	@Autowired
	GroupBuyParticipationRepository participationRepository;

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

		User hostUser = userRepository.findById(hostUserId).orElseThrow(() -> new RuntimeException("查無此會員"));
		GroupBuyVO groupBuyVO = new GroupBuyVO();
		groupBuyVO.setHostUser(hostUser);
		groupBuyVO.setProduct(product);
		groupBuyVO.setTargetAmount(form.getTargetAmount());
		groupBuyVO.setOpenDatetime(form.getOpenDatetime());
		groupBuyVO.setDdlDatetime(form.getDdlDatetime());
		groupBuyVO.setPickupAddress(form.getPickupAddress());
		// 系統預設
		groupBuyVO.setRequestStatus(RequestStatus.pending);
		groupBuyVO.setStatus(GroupBuyStatus.pending);
		groupBuyVO.setRequestDatetime(new Timestamp(System.currentTimeMillis()));
		groupBuyVO.setGroupPrice(product.getGroupPrice());

		repository.save(groupBuyVO);
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
	
	
	//給一般消費者抓的單筆團購資料
	public GroupBuyDetailDTO getOneGroupBuyDetail(Integer groupBuyId) {
	    GroupBuyVO gb = repository.findById(groupBuyId)
	            .orElseThrow(() -> new RuntimeException("查無此團購"));

	    return new GroupBuyDetailDTO(
	            gb.getGroupBuyId(),
	            gb.getProduct().getProductName(),
	            gb.getGroupPrice(),
	            gb.getTargetAmount(),
	            gb.getOpenDatetime(),
	            gb.getDdlDatetime(),
	            gb.getPickupAddress(),
	            gb.getStatus()
	    );
	}
	

	// 給小農前台查看團購清單用
	// 查資料庫的 GroupBuyVO->轉成前端要看的 GroupBuyFarmerDTO->回傳給小農頁面(把資料庫查到的
	// GroupBuyVO清單，轉成前端要看的 GroupBuyFarmerDTO 清單)
	@Transactional(readOnly = true)
	public List<GroupBuyFarmerDTO> showGroupBuyList(Integer farmerId) {
		List<GroupBuyVO> list = repository.findByProduct_FarmerId(farmerId);
		return list.stream()
				.map(gb -> new GroupBuyFarmerDTO(gb.getGroupBuyId(), gb.getProduct().getProductId(),
						gb.getProduct().getProductName(), gb.getGroupPrice(), gb.getHostUser().getUserName(),
						gb.getTargetAmount(), gb.getOpenDatetime(), gb.getDdlDatetime(), gb.getPickupAddress(),
						gb.getRequestStatus(), gb.getRequestDatetime(), gb.getStatus(), gb.getReplyDatetime(),
						gb.getRejectReason()))
				.toList();
	}

	// 給管理員的總表
	@Transactional(readOnly = true)
	public List<GroupBuyVO> showAllGroupBuyToAdmin() {
		return repository.findAll();
	}
	
	
	public GroupBuyOrderDTO showOrder(Integer orderId) {
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
	    dto.setTrackingNum(vo.getTrackingNum());
	    dto.setCreatedAt(vo.getCreatedAt());
	    dto.setReceivedAt(vo.getReceivedAt());
	    dto.setOrderStatus(vo.getOrderStatus());
	    dto.setPaidStatus(vo.getPaidStatus());
	    dto.setCompletedAt(vo.getCompletedAt());

	    return dto;
	}
	

	public List<GroupBuyVO> getOpenGroupBuys() {
		return repository.findByRequestStatus(RequestStatus.approved, GroupBuyStatus.open);
	}

	public GroupBuyVO getOneGroupBuyId(Integer groupBuyId) {
		return repository.findById(groupBuyId).orElse(null);
	}

	public GroupBuyParticipationVO getGroupBuyId(Integer groupBuyId) {
		return participationRepository.findById(groupBuyId).orElse(null);
	}
	// 顯示可團購之商品
	public List<ProductGroupBuyDTO> getConsumerGroupBuyList() {
		List<GroupBuyVO> groupBuyList = repository.findByRequestStatus(RequestStatus.approved, GroupBuyStatus.open);
		return groupBuyList.stream()
				.map(groupBuy -> new ProductGroupBuyDTO(groupBuy.getGroupBuyId(), groupBuy.getProduct().getProductId(),
						groupBuy.getProduct().getProductName(), groupBuy.getGroupPrice(), groupBuy.getTargetAmount(),
						groupBuy.getOpenDatetime(), groupBuy.getDdlDatetime(), groupBuy.getPickupAddress(),
						groupBuy.getStatus()))
				.toList();
	}
}