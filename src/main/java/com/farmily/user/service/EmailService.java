package com.farmily.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.farmily.coupon.model.CouponVO;

// 負責處理信件內容 (驗證+重設密碼)
@Service
public class EmailService {

    // 寄件者信箱（用 application.properties 設定的帳號）
    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 寄出會員驗證信
    // @Async：另開執行緒寄信，不要卡住註冊的回應
    @Async
    public void sendVerifyEmail(String toEmail, String verifyLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily 帳號 - Email 驗證");
        message.setText("您好，\n\n"
                + "請點擊以下連結完成 Email 驗證（連結 24 小時內有效）：\n"
                + verifyLink);

        mailSender.send(message);
    }

    // 寄出小農啟用驗證信件
    @Async
    public void sendFarmerVerifyEmail(String toEmail, String verifyLink){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily 小農帳號啟用通知");
        message.setText("您好，\n\n"
                + "您申請的小農帳號已審核通過！\n"
                + "請點擊以下連結，驗證並啟用小農帳號（連結 24 小時內有效）：\n"
                + verifyLink);

        mailSender.send(message);
    }

    // 寄出小農審核「退件」通知信
    @Async
    public void sendFarmerRejectedEmail(String toEmail, String rejectReason, String applicationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily 小農申請審核結果通知");
        message.setText("您好，\n\n"
                + "很抱歉，您申請的小農帳號本次審核未通過。\n"
                + "退件理由：" + (rejectReason == null || rejectReason.isBlank() ? "(未提供)" : rejectReason) + "\n\n"
                + "您可以點擊以下連結查看審核進度或重新申請：\n"
                + applicationLink + "\n\n"
                + "若有任何疑問，請聯繫客服：supportfarmily@gmail.com\n");

        mailSender.send(message);
    }

    // 寄出重設密碼信 (會員/小農)
    @Async
    public void sendResetPasswordEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily - 重設密碼");
        message.setText("您好，\n\n"
                + "請點擊以下連結重設您的密碼（連結 30 分鐘內有效）：\n"
                + resetLink);

        mailSender.send(message);
    }

    // 寄出「密碼已變更」純通知信（會員/小農）
    @Async
    public void sendPasswordChangedNotice(String toEmail) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily - 密碼變更通知");
        message.setText("您好，\n\n"
                + "您的 Farmily 帳號密碼剛剛已被變更，若為本人操作，請忽略此信。\n"
                + "若非本人操作，您的帳號可能有風險，請立即聯繫客服：supportfarmily@gmail.com\n");

        mailSender.send(message);
    }

    // 寄出「密碼已變更」純通知信（管理員）
    @Async
    public void sendPasswordChangedNoticeToAdmin(String toAdminEmail) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toAdminEmail);
        message.setSubject("Farmily 管理員團隊 - 密碼變更通知");
        message.setText("您的管理員密碼剛剛已變更，請使用新密碼登入。\n");

        mailSender.send(message);
    }
    //Coupon~~mail
    @Async
    public void sendCouponEmail(String toEmail,String userName,CouponVO coupon) {
    		//建立SimpleMailMessage物件
    		SimpleMailMessage message = new SimpleMailMessage();
    		message.setFrom(fromEmail);
    		message.setTo(toEmail);
    		message.setSubject("優惠卷通知");
    		message.setText(userName+"有新的優惠卷"+"券號是:"+coupon.getCouponId()+"折抵金額：" + coupon.getAmount()+ "最低消費：" + coupon.getMinSpending());
    		mailSender.send(message);
    	}
    
 // 寄出「體驗活動場次取消」通知信給已報名的會員
    @Async
    public void sendTripSessionCancelledEmail(String toEmail, String userName, String tripTitle, String sessionTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Farmily - 體驗活動場次取消通知");
        message.setText("您好 " + (userName == null || userName.isBlank() ? "" : userName) + "，\n\n"
                + "很抱歉通知您，您報名的體驗活動「" + tripTitle + "」"
                + (sessionTime == null || sessionTime.isBlank() ? "" : "（場次時間：" + sessionTime + "）")
                + "已由主辦小農取消，此筆報名同時已為您取消。\n"
                + "造成您的不便，我們深感抱歉。\n\n"
                + "若有任何疑問，請聯繫客服：supportfarmily@gmail.com\n");

        mailSender.send(message);
    }

    // 小農主動寄給報名者的「活動提醒」信（主旨與內文由小農自訂）
    @Async
    public void sendTripReminderEmail(String toEmail, String userName, String tripTitle,
            String sessionTime, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject == null || subject.isBlank() ? "Farmily - 體驗活動提醒" : subject);
        message.setText("您好 " + (userName == null || userName.isBlank() ? "" : userName) + "，\n\n"
                + "這是您報名的體驗活動「" + tripTitle + "」"
                + (sessionTime == null || sessionTime.isBlank() ? "" : "（場次時間：" + sessionTime + "）")
                + " 主辦小農的提醒：\n\n"
                + body + "\n\n"
                + "期待與您相見！\n"
                + "若有任何疑問，請聯繫客服：supportfarmily@gmail.com\n");

        mailSender.send(message);
    }
    
}