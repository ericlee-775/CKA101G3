package com.farmily.user.event;

import com.farmily.user.service.EmailVerificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 建會員註冊不可逆事件監聽器 (寄信通知)
@Component
public class MemberRegisteredListener {

    private final EmailVerificationService emailVerificationService;

    public MemberRegisteredListener(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    // 只有「交易成功 commit 後」寄信通知方法才會被呼叫
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberRegistered(MemberRegisteredEvent event) {
        emailVerificationService.sendVerification(event.getEmail(), event.getAccountType());
    }
}