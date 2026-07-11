package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.FarmerUserDetails;
import com.farmily.user.security.service.FarmerUserDetailsService;
import com.farmily.user.service.FarmerService;
import com.farmily.user.service.SessionService;
import com.farmily.user.util.SessionCookieSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    private final FarmerService farmerService;
    private final FarmerUserDetailsService farmerUserDetailsService;
    private final SessionService sessionService;
    private final SessionCookieSupport sessionCookieSupport;

    public FarmerController(FarmerService farmerService,
                            FarmerUserDetailsService farmerUserDetailsService,
                            SessionService sessionService,
                            SessionCookieSupport sessionCookieSupport) {
        this.farmerService = farmerService;
        this.farmerUserDetailsService = farmerUserDetailsService;
        this.sessionService = sessionService;
        this.sessionCookieSupport = sessionCookieSupport;
    }

    // 小農註冊申請（含證明文件圖片，走 multipart/form-data）
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FarmerProfileResponse> register(
            @ModelAttribute @Valid FarmerRegisterRequest reg) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(farmerService.register(reg));
    }

    // 小農登入
    @PostMapping("/login")
    public ResponseEntity<FarmerProfileResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // step1: 呼叫 Service，判斷帳號狀態、比對密碼，回傳 dto
        FarmerProfileResponse response = farmerService.login(req);

        // step2: 通知 Spring Security 此人已通過驗證
        UserDetails userDetails = farmerUserDetailsService.loadUserByUsername(req.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // step3: 把 SecurityContext 存進 HttpSession，後續請求才能持續認得他
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        // 把這個 session 登記進來（手動登入不會自動登記），之後改/重設密碼才找得到並讓它失效
        sessionService.registerSession(session.getId(), userDetails);

        // step4: 依「記住我」決定 server session 壽命 + JSESSIONID cookie 是否寫進硬碟
        sessionCookieSupport.applyRememberMe(session, httpResponse, req.isRememberMe());

        return ResponseEntity.ok(response);
    }

    // 查自己資料 - @AuthenticationPrincipal 取出登入者本人
    @GetMapping("/me")
    public ResponseEntity<FarmerProfileResponse> getMe(
            @AuthenticationPrincipal FarmerUserDetails me) {
        return ResponseEntity.ok(farmerService.getMyProfile(me.getFarmerId()));
    }

    // 修改非審核欄位（電話、描述) - 立即生效
    @PutMapping("/me")
    public ResponseEntity<FarmerProfileResponse> updateContactInfo(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestBody @Valid FarmerProfileUpdateRequest req) {
        FarmerProfileResponse response = farmerService.updateContactInfo(me.getFarmerId(), req);
        return ResponseEntity.ok(response);
    }

    // 修改審核相關欄位 - 重新送審（含證明文件圖片，走 multipart/form-data；沒上傳則沿用上一輪）
    // 用 POST 而非 PUT：Tomcat 只自動解析 POST 的 multipart，PUT+multipart 會在進 controller 前失敗（表現為 403）
    @PostMapping(value = "/me/application", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FarmerProfileResponse> updateReviewRequiredInfo(
            @AuthenticationPrincipal FarmerUserDetails me,
            @ModelAttribute @Valid FarmerResubmitRequest req) {
        FarmerProfileResponse response = farmerService.updateReviewRequiredInfo(me.getFarmerId(), req);
        return ResponseEntity.ok(response);
    }

    // 修改自己密碼
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal FarmerUserDetails me,
            @RequestBody @Valid ChangePasswordRequest pw,
            HttpServletRequest request) {
        // 改密碼成功後由 service 寄出密碼變更通知信
        farmerService.changePassword(me.getFarmerId(), pw);

        // 踢掉「其他裝置」的 session（保留自己這台）
        HttpSession session = request.getSession(false);
        String currentSessionId = null;
        if (session != null) {
            currentSessionId = session.getId();
        }
        sessionService.expireSessions(me.getUsername(), currentSessionId);

        return ResponseEntity.ok("密碼修改成功！其他裝置已登出");
    }

}
