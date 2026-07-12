package com.farmily.user.event;

import com.farmily.user.service.EmailVerificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 寄信通知小農審核「通過、退件」，不可逆事件監聽器 (寄信通知)
@Component
public class FarmerReviewListener {

    private final EmailVerificationService emailVerificationService;

    public FarmerReviewListener(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    // 事件相互獨立，不需要 @Order
    // 審核通過：commit 後才寄「啟用信」
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFarmerApproved(FarmerApprovedEvent event) {
        emailVerificationService.sendFarmerActivation(event.getEmail());
    }

    // 退件：commit 後才寄「退件通知信」
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFarmerRejected(FarmerRejectedEvent event) {
        emailVerificationService.sendFarmerRejected(event.getEmail(), event.getRejectReason());
    }
}