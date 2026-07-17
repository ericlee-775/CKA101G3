package com.farmily.notification.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.notification.dto.NotificationCreateRequestDTO;
import com.farmily.notification.model.NotificationRecipientType;


// 給其他功能發送通知用的方法們
@Service
public class NotificationSender {
	
	@Autowired
	private NotificationService nSvc;

	/*
	 * sendOneNotif(String typeCode, NotificationRecipientType recipientType, 
	 * Integer recipientId, String targetType, Integer targetId, Map<String, String> variables)
	 * 
	 * sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, 
	 * String typeCode, String targetType, Integer targetId, Map<String, String> variables) {
	*/

	// ====================== 商品訂單 ======================
	// 新商品訂單成立，通知小農/會員
	@Transactional
	public void sendProdOrderCreated(Set<Integer> farmerIds, Integer userId, Integer orderId) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		// 給消費者 (會員)
		req.setTypeCode("order_created");
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("order");
		req.setTargetId(orderId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId)));
		nSvc.sendOneNotif(req);
		
		// 給小農們 
		// sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, String typeCode, String targetType, Integer targetId, Map<String, String> variables) 
		nSvc.sendMultipleNotif(farmerIds, NotificationRecipientType.farmer, "order_farmer_new", "order", orderId, Map.of("order_id", String.valueOf(orderId)));
	}
	
	
	// 訂單出貨	order_shipped, 一般會員 userId
	@Transactional
	public void sendProdOrderShipped(Integer userId, Integer orderId, LocalDateTime shippedAt) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("order_shipped");
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("order");
		req.setTargetId(orderId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId), "shipped_at", shippedAt.format(fmt)));
		nSvc.sendOneNotif(req);
	}
	
	// 訂單確認收貨	order_received, 一般會員 userId
	@Transactional
	public void sendProdOrderReceived(Integer userId, Integer orderId, LocalDateTime receivedAt) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("order_received");
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("order");
		req.setTargetId(orderId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId), "received_at", receivedAt.format(fmt)));
		nSvc.sendOneNotif(req);
	}
	
	// 訂單已撥款	order_payout, 小農 farmerId
	@Transactional
	public void sendProdOrderPayout(Integer farmerId, Integer orderId) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("order_payout");
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("order");
		req.setTargetId(orderId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId)));
		nSvc.sendOneNotif(req);
	}
	
	
	
	
	
	// ====================== 團購 ======================
	// 團購申請通過	gb_request_approved, 通知團主(hostUserId)ok
	@Transactional
	public void sendGBApproved(Integer hostUserId, Integer groupBuyId) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("gb_request_approved");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(hostUserId);
		req.setTargetType("groupbuy");
		req.setTargetId(groupBuyId);
		req.setVariables(Map.of("group_buy_id", String.valueOf(groupBuyId)));
		nSvc.sendOneNotif(req);
	}
	
	// 團購申請被拒	gb_request_rejected, 通知團主(hostUserId)ok
	@Transactional
	public void sendGBRejected(Integer hostUserId, Integer groupBuyId, String rejectReason) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("gb_request_rejected");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(hostUserId);
		req.setTargetType("groupbuy");
		req.setTargetId(groupBuyId);
		req.setVariables(Map.of("group_buy_id", String.valueOf(groupBuyId), "reject_reason", rejectReason));
		nSvc.sendOneNotif(req);
	}
	
	// 團購達標成團	gb_success, 所有參與者 userId ok
	@Transactional
	public void sendGBSuccess(Set<Integer> userIds, Integer groupBuyId, Integer orderId) {

		// sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, String typeCode, 
		// String targetType, Integer targetId, Map<String, String> variables) 
		nSvc.sendMultipleNotif(userIds, NotificationRecipientType.user, "gb_success", 
				"groupbuy", groupBuyId, Map.of("group_buy_id", String.valueOf(groupBuyId), "order_id", String.valueOf(orderId)));
		
	}
	
	// 團購未達標	gb_failed, 所有參與者 userId ok
	@Transactional
	public void sendGBFailed(Set<Integer> userIds, Integer groupBuyId) {

		// sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, String typeCode, 
		// String targetType, Integer targetId, Map<String, String> variables) 
		nSvc.sendMultipleNotif(userIds, NotificationRecipientType.user, "gb_failed", 
				"groupbuy", groupBuyId, Map.of("group_buy_id", String.valueOf(groupBuyId)));
		
	}
	
	// 團購取消	gb_cancelled, 所有參與者 userId x
	
	@Transactional
	public void sendGBCancelled(Set<Integer> userIds, Integer groupBuyId) {

		// sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, String typeCode, 
		// String targetType, Integer targetId, Map<String, String> variables) 
		nSvc.sendMultipleNotif(userIds, NotificationRecipientType.user, "gb_cancelled", 
				"groupbuy", groupBuyId, Map.of("group_buy_id", String.valueOf(groupBuyId)));
		
	}
	
	// 團購訂單出貨	gb_shipped, 團主 host x
	@Transactional
	public void sendGBShipped(Integer hostUserId, Integer groupBuyId, Integer orderId, LocalDateTime shippedAt) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("gb_shipped");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(hostUserId);
		req.setTargetType("groupbuy");
		req.setTargetId(groupBuyId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId), "shipped_at", shippedAt.format(fmt)));
		nSvc.sendOneNotif(req);
	}
	
	// 團購訂單送達	gb_delivered, 團主 host ok
	@Transactional
	public void sendGBDelivered(Integer hostUserId, Integer groupBuyId, Integer orderId, LocalDateTime receivedAt) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("gb_delivered");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(hostUserId);
		req.setTargetType("groupbuy");
		req.setTargetId(groupBuyId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId), "received_at", receivedAt.format(fmt)));
		nSvc.sendOneNotif(req);
	}
	
	// 團購發起請求	gb_request_created, 小農 farmerId ok
	@Transactional
	public void sendGBRequestCreated(Integer farmerId, Integer groupBuyId, String productName) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("gb_request_created");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("groupbuy");
		req.setTargetId(groupBuyId);
		req.setVariables(Map.of("product_name", String.valueOf(productName)));
		nSvc.sendOneNotif(req);
	}
	
	// 團購成團出貨	gb_order_created, 小農 farmerId ok
	@Transactional
	public void sendGBOrderCreated(Integer farmerId, Integer groupBuyId) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("gb_order_created");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("groupbuy");
		req.setTargetId(groupBuyId);
		req.setVariables(Map.of("group_buy_id", String.valueOf(groupBuyId)));
		nSvc.sendOneNotif(req);
	}
	
	// 團購撥款	gb_payout, 小農 farmerId ok
	@Transactional 
	public void sendGBPayout(Integer farmerId, Integer groupBuyId, Integer orderId, Integer totalAmount) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("gb_payout");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("groupbuy");
		req.setTargetId(groupBuyId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId), "total_amount", String.valueOf(totalAmount)));
		nSvc.sendOneNotif(req);
	}
	
	
	
	
	// 日期時間格式轉換 (String shippedStr = shippedAt.format(fmt); // "2026-07-06 19:53" )
	private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	
}
