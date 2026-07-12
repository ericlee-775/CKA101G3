package com.farmily.user.event;

import com.farmily.user.service.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 修改密碼寄信通知事件監聽
@Component
public class PasswordChangedListener {

    private final EmailService emailService;

    public PasswordChangedListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordChanged(PasswordChangedEvent event) {
        emailService.sendPasswordChangedNotice(event.getEmail());
    }
}