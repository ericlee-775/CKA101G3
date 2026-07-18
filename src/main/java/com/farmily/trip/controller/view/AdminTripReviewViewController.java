package com.farmily.trip.controller.view;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.farmily.notification.model.NotificationRecipientType;
import com.farmily.notification.model.NotificationVO;
import com.farmily.notification.service.NotificationService;
import com.farmily.trip.dto.TripReviewRequest;
import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.repository.FarmTripRepository;
import com.farmily.trip.service.FarmTripService;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.notification.service.NotificationSender;

// 管理後台－活動審核頁（Thymeleaf）
@Controller
@RequestMapping("/admin/farm-trips")
public class AdminTripReviewViewController {

	private final FarmTripService farmTripService;
    private final FarmTripRepository farmTripRepository;
    private final NotificationService notificationService;
    private final NotificationSender notificationSender;

    public AdminTripReviewViewController(FarmTripService farmTripService,
                                         FarmTripRepository farmTripRepository,
                                         NotificationService notificationService,
                                         NotificationSender notificationSender) {
        this.farmTripService = farmTripService;
        this.farmTripRepository = farmTripRepository;
        this.notificationService = notificationService;
        this.notificationSender = notificationSender;
    }

    // 審核頁：列出所有待審核活動
    @GetMapping
    public String list(ModelMap model) {
        return backToList(model, null, null);
    }

    // 核准上架（PENDING -> ACTIVE）
    @PostMapping("/{farmTripId}/approve")
    public String approve(@PathVariable Integer farmTripId,
                          @AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        TripReviewRequest req = new TripReviewRequest();
        req.setApproved(true);
        req.setAdminId(me.getAdminId());
        farmTripService.reviewTrip(farmTripId, req);   // 審核＋上架（交易完成後才發通知）

        try {
            Integer farmerId = farmTripRepository.findById(farmTripId).map(FarmTrip::getFarmerId).orElse(null);
            if (farmerId != null) notificationSender.sendTripRequestApproved(farmerId, farmTripId);
        } catch (Exception e) { System.out.println("[通知] 審核通過通知失敗：" + e.getMessage()); }          // 通知小農

        return backToList(model, "success", "已核准，此活動審核成功並上架！");
    }

    // 退回（PENDING -> REJECTED，需填理由）
    @PostMapping("/{farmTripId}/reject")
    public String reject(@PathVariable Integer farmTripId,
                         @RequestParam("rejectReason") String rejectReason,
                         @AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        TripReviewRequest req = new TripReviewRequest();
        req.setApproved(false);
        req.setComment(rejectReason);
        req.setAdminId(me.getAdminId());
        farmTripService.reviewTrip(farmTripId, req);

        try {
            Integer farmerId = farmTripRepository.findById(farmTripId).map(FarmTrip::getFarmerId).orElse(null);
            if (farmerId != null) notificationSender.sendTripRequestRejected(farmerId, farmTripId, rejectReason);
        } catch (Exception e) { System.out.println("[通知] 審核退回通知失敗：" + e.getMessage()); }
        return backToList(model, "warning", "已退回此活動。");
    }

    // 活動圖片：讓審核頁 <img> 直接內嵌
    @GetMapping("/{farmTripId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> image(@PathVariable Integer farmTripId) throws IOException {
        byte[] img = farmTripService.getTripImage(farmTripId);
        if (img == null || img.length == 0) {
            return ResponseEntity.notFound().build();
        }
        String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(img));
        MediaType mediaType = (type != null) ? MediaType.parseMediaType(type) : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).body(img);
    }

    // 發通知給小農（包在 try/catch：通知失敗「絕不」影響審核結果）
    private void notifyFarmer(Integer farmTripId, boolean approved, String rejectReason) {
        try {
            FarmTrip trip = farmTripRepository.findById(farmTripId).orElse(null);
            if (trip == null || trip.getFarmerId() == null) return;

            String content = approved
                    ? "您的體驗活動「" + trip.getFarmTripTitle() + "」已審核通過並上架！"
                    : "您的體驗活動「" + trip.getFarmTripTitle() + "」未通過審核。原因：" + rejectReason;

            NotificationVO notif = new NotificationVO();
            notif.setRecipientType(NotificationRecipientType.farmer);
            notif.setRecipientId(trip.getFarmerId());
            notif.setTargetType("FARM_TRIP");
            notif.setTargetId(farmTripId);
            notif.setContent(content);
            notificationService.addNotif(notif);
        } catch (Exception ex) {
            // 通知失敗不擋審核；若小農收不到，請與通知模組負責人確認 notification 資料表欄位
        }
    }

    // 私有小工具：重查待審清單 + 帶提示訊息，回審核頁
    private String backToList(ModelMap model, String alertType, String alertMsg) {
        model.addAttribute("pendingTrips", farmTripService.getPendingTrips());
        if (alertMsg != null) {
            model.addAttribute("alertType", alertType);
            model.addAttribute("alertMsg", alertMsg);
        }
        return "back-end/admin/listFarmTripReview";
    }
}