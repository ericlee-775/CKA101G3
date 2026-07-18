package com.farmily.trip.service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.notification.service.NotificationSender;
import com.farmily.trip.model.FarmTripOrder;
import com.farmily.trip.model.FarmTripSession;
import com.farmily.trip.model.OrderStatus;
import com.farmily.trip.model.SessionStatus;
import com.farmily.trip.repository.FarmTripOrderRepository;
import com.farmily.trip.repository.FarmTripSessionRepository;

@Component
public class FarmTripReviewInviteScheduler {

	private final FarmTripSessionRepository sessionRepo;
	private final FarmTripOrderRepository orderRepo;
	private final NotificationSender notificationSender;

	public FarmTripReviewInviteScheduler(FarmTripSessionRepository sessionRepo,
			FarmTripOrderRepository orderRepo, NotificationSender notificationSender) {
		this.sessionRepo = sessionRepo;
		this.orderRepo = orderRepo;
		this.notificationSender = notificationSender;
	}

	// 每 10 分鐘檢查一次；已結束且還沒邀評的場次 → 發邀評、標記完成
	@Scheduled(cron = "0 */10 * * * *")
	@Transactional
	public void inviteReviewsForEndedSessions() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		List<FarmTripSession> ended = sessionRepo.findBySessionStatusAndFarmTripEndBefore(SessionStatus.ACTIVE, now);
		for (FarmTripSession s : ended) {
			Set<Integer> userIds = new HashSet<>();
			Integer anyOrderId = null;
			for (FarmTripOrder o : orderRepo.findByFarmSessionId(s.getFarmSessionId())) {
				if (o.getOrderStatus() == OrderStatus.CONFIRMED && o.getUserId() != null) {
					userIds.add(o.getUserId());
					anyOrderId = o.getFarmTripOrderId();
				}
			}
			if (!userIds.isEmpty()) {
				try {
					notificationSender.sendTripReviewInvite(userIds, s.getFarmSessionId(), anyOrderId);
				} catch (Exception e) {
					System.out.println("[通知] 邀評失敗：" + e.getMessage());
				}
			}
			// 標記為已完成，下次就不會重複邀評
			s.setSessionStatus(SessionStatus.COMPLETED);
			sessionRepo.save(s);
		}
	}
}