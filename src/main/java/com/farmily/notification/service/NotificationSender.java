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
				"groupbuy", orderId, Map.of("group_buy_id", String.valueOf(groupBuyId), "order_id", String.valueOf(orderId)));
		
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
		req.setTargetId(orderId);
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
		req.setTargetId(orderId);
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
		req.setTargetId(orderId);
		req.setVariables(Map.of("order_id", String.valueOf(orderId), "total_amount", String.valueOf(totalAmount)));
		nSvc.sendOneNotif(req);
	}
	
	
	// ====================== 體驗活動 ======================
	// 體驗活動審核通過	trip_request_approved, 小農 farmerId
	@Transactional
	public void sendTripRequestApproved(Integer farmerId, Integer farmTripId) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("trip_request_approved");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("trip");
		req.setTargetId(farmTripId);
		req.setVariables(Map.of("farm_trip_id", String.valueOf(farmTripId)));
		nSvc.sendOneNotif(req);
	}
	
	// 體驗活動審核未通過	trip_request_rejected, 小農 farmerId
	@Transactional
	public void sendTripRequestRejected(Integer farmerId, Integer farmTripId, String reason) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("trip_request_rejected");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("trip");
		req.setTargetId(farmTripId);
		req.setVariables(Map.of("farm_trip_id", String.valueOf(farmTripId), "reason", reason));
		nSvc.sendOneNotif(req);
	}
	
	// 體驗活動預約成功	trip_booking_confirmed, 一般會員 userId
	@Transactional
	public void sendTripBookConfirmed(Integer userId, Integer farmerId, Integer farmSessionId, Integer farmTripOrderId) {
		
		// 發送通知給會員
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("trip_booking_confirmed");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("trip");
		req.setTargetId(farmTripOrderId);
		req.setVariables(Map.of("farm_trip_order_id", String.valueOf(farmTripOrderId)));
		nSvc.sendOneNotif(req);
		
		// 發送通知給小農
		NotificationCreateRequestDTO reqFarmer = new NotificationCreateRequestDTO();
		reqFarmer.setTypeCode("trip_booking_new");			
		reqFarmer.setRecipientType(NotificationRecipientType.farmer);
		reqFarmer.setRecipientId(farmerId);
		reqFarmer.setTargetType("trip");
		reqFarmer.setTargetId(farmSessionId);
		reqFarmer.setVariables(Map.of("farm_session_id", String.valueOf(farmSessionId)));
		nSvc.sendOneNotif(reqFarmer);
	}
	
	// 體驗活動取消預約	trip_booking_cancelled, 一般會員 userId
	@Transactional
	public void sendTripBookCancelled(Integer userId, Integer farmSessionId, Integer farmTripOrderId) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("trip_booking_cancelled");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("trip");
		req.setTargetId(farmTripOrderId);
		req.setVariables(Map.of("farm_trip_order_id", String.valueOf(farmTripOrderId)));
		nSvc.sendOneNotif(req);
	}

	// 體驗活動取消	trip_cancelled, 所有參與者 userId
	@Transactional
	public void sendTripCancelled(Set<Integer> userIds, Integer farmSessionId, Integer farmTripOrderId) {
		// sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, 
		// String typeCode, String targetType, Integer targetId, Map<String, String> variables) 
		nSvc.sendMultipleNotif(userIds, NotificationRecipientType.user, 
				"trip_cancelled", "trip", farmTripOrderId, Map.of("farm_session_id", String.valueOf(farmSessionId)));
	}
	
	// 體驗活動完成	trip_review_invite, 所有參與者 userId
	@Transactional
	public void sendTripReviewInvite(Set<Integer> userIds, Integer farmSessionId, Integer farmTripOrderId) {
		// sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, 
		// String typeCode, String targetType, Integer targetId, Map<String, String> variables) 
		nSvc.sendMultipleNotif(userIds, NotificationRecipientType.user, 
				"trip_review_invite", "trip", farmSessionId, Map.of("farm_session_id", String.valueOf(farmSessionId)));
	}
	
	// 體驗活動評論	trip_comment, 小農 farmerId
	@Transactional
	public void sendTripComment(Integer farmerId, Integer farmTripId) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("trip_comment");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("trip");
		req.setTargetId(farmTripId);
		req.setVariables(Map.of("farm_trip_id", String.valueOf(farmTripId)));
		nSvc.sendOneNotif(req);
	}
	
	
	// ====================== 專欄部落格 ======================
	// 專欄文章被檢舉	blog_report_pending, 一般會員 userId
	@Transactional
	public void sendBlogReportPending(Integer userId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_report_pending");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", String.valueOf(blogTitle)));
		nSvc.sendOneNotif(req);
	}
	
	// 專欄文章被檢舉	blog_report_pending, 小農 farmerId
	@Transactional
	public void sendFarmerBlogReportPending(Integer farmerId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_report_pending");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", String.valueOf(blogTitle)));
		nSvc.sendOneNotif(req);
	}
	
	
	// 專欄文章被檢舉維持顯示	blog_report_visible, 一般會員 userId
	@Transactional
	public void sendBlogReportVisible(Integer userId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_report_visible");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", String.valueOf(blogTitle)));
		nSvc.sendOneNotif(req);
	}
	
	// 專欄文章被檢舉維持顯示	blog_report_visible, 小農 farmerId
	@Transactional
	public void sendFarmerBlogReportVisible(Integer farmerId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_report_visible");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", String.valueOf(blogTitle)));
		nSvc.sendOneNotif(req);
	}
	
	
	// 專欄文章被檢舉隱藏	blog_report_hidden, 一般會員 userId
	@Transactional
	public void sendBlogReportHidden(Integer userId, Integer blogId, String reportReason, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_report_hidden");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", blogTitle, "report_reason", reportReason));
		nSvc.sendOneNotif(req);
	}
	
	// 專欄文章被檢舉隱藏	blog_report_hidden, 小農 farmerId
	@Transactional
	public void sendFarmerBlogReportHidden(Integer farmerId, Integer blogId, String reportReason, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_report_hidden");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", blogTitle, "report_reason", reportReason));
		nSvc.sendOneNotif(req);
	}
	
	
	// 專欄文章評論	blog_comment, 一般會員 userId
	@Transactional
	public void sendBlogComment(Integer userId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_comment");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", blogTitle));
		nSvc.sendOneNotif(req);
	}
	
	// 專欄文章評論	blog_comment, 小農 farmerId
	@Transactional
	public void sendFarmerBlogComment(Integer farmerId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_comment");			
		req.setRecipientType(NotificationRecipientType.farmer);
		req.setRecipientId(farmerId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", blogTitle));
		nSvc.sendOneNotif(req);
	}
	
	
	// 專欄文章評論被檢舉	blog_comment_report, 一般會員 userId
	@Transactional
	public void sendBlogCommentReport(Integer userId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_comment_report");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", blogTitle));
		nSvc.sendOneNotif(req);
	}
	
	// 專欄文章評論被檢舉維持顯示	blog_comment_report_visible, 一般會員 userId
	@Transactional
	public void sendBlogCommentReportVisible(Integer userId, Integer blogId, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_comment_report_visible");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", blogTitle));
		nSvc.sendOneNotif(req);
	}
	
	// 專欄文章評論被檢舉隱藏	blog_comment_report_hidden, 一般會員 userId
	@Transactional
	public void sendBlogCommentReportHidden(Integer userId, Integer blogId, String reportReason, String blogTitle) {
		NotificationCreateRequestDTO req = new NotificationCreateRequestDTO();
		req.setTypeCode("blog_comment_report_hidden");			
		req.setRecipientType(NotificationRecipientType.user);
		req.setRecipientId(userId);
		req.setTargetType("blog");
		req.setTargetId(blogId);
		req.setVariables(Map.of("blog_title", blogTitle, "report_reason", reportReason));
		nSvc.sendOneNotif(req);
	}
	
	
	
	
	
	// 日期時間格式轉換 (String shippedStr = shippedAt.format(fmt); // "2026-07-06 19:53" )
	private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	
}
