package com.farmily.user.event;

import com.farmily.user.service.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 監聽:註銷帳號寄信通知事件
@Component
public class DeleteAccountListener {
    private final EmailService emailService;

    public DeleteAccountListener(EmailService emailService){
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleteAccount(DeleteAccountEvent event) {
        emailService.sendDeleteAccountNotice(event.getEmail());
    }
}
