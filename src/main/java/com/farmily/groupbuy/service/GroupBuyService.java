package com.farmily.groupbuy.service;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.groupbuy.model.GroupBuyFarmerDTO;
import com.farmily.groupbuy.model.GroupBuyHostCreateDTO;
import com.farmily.groupbuy.model.GroupBuyRepository;
import com.farmily.groupbuy.model.GroupBuyStatus;
import com.farmily.groupbuy.model.GroupBuyVO;
import com.farmily.groupbuy.model.RequestStatus;
import com.farmily.product.model.ProductVO;
import com.farmily.product.service.ProductServiceImpl;


@Service
public class GroupBuyService {
	@Autowired
	GroupBuyRepository repository;
	
	@Autowired
	ProductServiceImpl productSvc;
	
	@Autowired
    private SessionFactory sessionFactory;



	// 團購主發起請求
	
	@Transactional
	public void hostRequest(GroupBuyHostCreateDTO form, Integer productId, Integer hostUserId) {
		ProductVO product=productSvc.getGroupBuyProductById(productId);
		if(product==null) {
			throw new RuntimeException("查無此商品");
		} // 防止有人更改網址
		GroupBuyVO groupBuyVO = new GroupBuyVO();
		groupBuyVO.setHostUserId(hostUserId);
		groupBuyVO.setProduct(product);
		groupBuyVO.setTargetAmount(form.getTargetAmount());
		groupBuyVO.setOpenDatetime(form.getOpenDatetime());
		groupBuyVO.setDdlDatetime(form.getDdlDatetime());
		groupBuyVO.setPickupAddress(form.getPickupAddress());
		// 以下為系統預設
		groupBuyVO.setRequestStatus(RequestStatus.pending);
		groupBuyVO.setStatus(GroupBuyStatus.pending);
		groupBuyVO.setRequestDatetime(new Timestamp(System.currentTimeMillis()));
		groupBuyVO.setGroupPrice(product.getGroupPrice());
		repository.save(groupBuyVO);
	}

	// 給小農審核團購申請用
	@Transactional
	public void reviewGroupBuy(Integer groupBuyId, RequestStatus requestStatus, String rejectReason) {
		GroupBuyVO groupBuyVO = repository.findById(groupBuyId).orElseThrow(() -> new RuntimeException("查無此團購申請"));
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
			groupBuyVO.setStatus(GroupBuyStatus.pending);
		} else if (requestStatus == RequestStatus.rejected) {
			groupBuyVO.setRejectReason(rejectReason);
			groupBuyVO.setStatus(GroupBuyStatus.cancelled);
		}
		repository.save(groupBuyVO);
	}

	//查資料庫的 GroupBuyVO->轉成前端要看的 GroupBuyFarmerDTO->回傳給小農頁面(把資料庫查到的 GroupBuyVO 清單，轉成前端要看的 GroupBuyFarmerDTO 清單)
	@Transactional(readOnly=true)
	public List<GroupBuyFarmerDTO> showGroupBuyList(Integer farmerId) {
		List<GroupBuyVO> list = repository.findByProduct_FarmerId(farmerId);
		return list.stream()
				.map(gb -> new GroupBuyFarmerDTO(gb.getGroupBuyId(), gb.getProduct().getProductId(),
						gb.getProduct().getProductName(), gb.getGroupPrice(), gb.getHostUserId(),
						gb.getTargetAmount(), gb.getOpenDatetime(), gb.getDdlDatetime(), gb.getPickupAddress(),
						gb.getRequestStatus(), gb.getStatus(), gb.getRequestDatetime(), gb.getReplyDatetime(),
						gb.getRejectReason()))
				.toList();
	}

	public List<GroupBuyVO> getOpenGroupBuys() {
		return repository.findByRequestStatus(RequestStatus.approved, GroupBuyStatus.open);
	}

	public GroupBuyVO getOneGroupBuyId(Integer groupBuyId) {
		return repository.findById(groupBuyId).orElse(null);
	}

	public List<GroupBuyVO> getAll() {
		return repository.findAll();
	}

}