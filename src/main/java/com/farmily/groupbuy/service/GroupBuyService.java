package com.farmily.groupbuy.service;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.groupbuy.model.GroupBuyHostCreate;
import com.farmily.groupbuy.model.GroupBuyRepository;
import com.farmily.groupbuy.model.GroupBuyStatus;
import com.farmily.groupbuy.model.GroupBuyVO;
import com.farmily.groupbuy.model.RequestStatus;
import com.farmily.product.model.ProductVO;
import com.farmily.product.service.ProductService;

import jakarta.transaction.Transactional;


@Service
public class GroupBuyService {

	@Autowired
	GroupBuyRepository repository;
	
	@Autowired
	ProductService productSvc;
	
	@Autowired
    private SessionFactory sessionFactory;

	@Transactional
	public void hostRequest(GroupBuyHostCreate form,Integer productId,Integer hostUserId) {
		
		ProductVO product=productSvc.getProductReferenceById(productId);
		if(product==null) {
			throw new RuntimeException("查無此商品");
		}
		GroupBuyVO groupBuyVO=new GroupBuyVO();
		groupBuyVO.setHostUserId(hostUserId);
		groupBuyVO.setProduct(product);
		groupBuyVO.setTargetAmount(form.getTargetAmount());
		groupBuyVO.setOpenDatetime(form.getOpenDatetime());
		groupBuyVO.setDdlDatetime(form.getDdlDatetime());
		groupBuyVO.setPickupAddress(form.getPickupAddress());
		
		//系統預設狀態
		groupBuyVO.setRequestStatus(RequestStatus.pending);
		groupBuyVO.setStatus(GroupBuyStatus.pending);
		groupBuyVO.setRequestDatetime(new Timestamp(System.currentTimeMillis()));
		groupBuyVO.setGroupPrice(product.getGroupPrice());
		repository.save(groupBuyVO);
	}
	
	


	@Transactional
	public void reviewGroupBuy(Integer groupBuyId, 
			RequestStatus requestStatus, String rejectReason) {
		
	    GroupBuyVO groupBuyVO = repository.findById(groupBuyId)
	            .orElseThrow(() -> new RuntimeException("查無此團購申請"));
	    if (requestStatus == null) {
	        throw new RuntimeException("請選擇審核結果");
	    }
	    if (groupBuyVO.getRequestStatus() == RequestStatus.approved ||
	        groupBuyVO.getRequestStatus() == RequestStatus.rejected) {
	        throw new RuntimeException("此團購申請已審核過，請勿重複審核");
	    }
	    if (requestStatus == RequestStatus.rejected &&
	        (rejectReason == null || rejectReason.trim().isEmpty())) {
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
	
	public List<GroupBuyVO> getOpenGroupBuys(){
		return repository.findByRequestStatus(RequestStatus.approved,GroupBuyStatus.open);
	}
	public GroupBuyVO getBuyGrouBuyId(Integer groupBuyId) {
		return repository.findById(groupBuyId).orElse(null);
	}
	
	
	
	public List<GroupBuyVO> getAll() {
		return repository.findAll();
	}
	
	



}