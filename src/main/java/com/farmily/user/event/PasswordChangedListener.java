package com.farmily.user.event;

import com.farmily.user.service.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 監聽:修改密碼寄信通知事件
@Component
public class PasswordChangedListener {
    private final EmailService emailService;

    public PasswordChangedListener(EmailService emailService) {
        this.emailService = emailService;
    }

    // 監聽會員/小農交易
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordChanged(PasswordChangedEvent event) {
        emailService.sendPasswordChangedNotice(event.getEmail());
    }

    // 監聽管理員交易
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdminPasswordChanged(AdminPasswordChangedEvent event) {
        emailService.sendPasswordChangedNoticeToAdmin(event.getEmail());
    }
}