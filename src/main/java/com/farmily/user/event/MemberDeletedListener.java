package com.farmily.user.event;

import com.farmily.user.service.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 會員註銷寄信通知事件監聽
@Component
public class MemberDeletedListener {

    private final EmailService emailService;

    public MemberDeletedListener(EmailService emailService) {
        this.emailService = emailService;
    }

    // 只有「交易成功 commit 後」才寄註銷通知信
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberDeleted(MemberDeletedEvent event) {
        emailService.sendAccountDeletedNotice(event.getEmail());
    }
}