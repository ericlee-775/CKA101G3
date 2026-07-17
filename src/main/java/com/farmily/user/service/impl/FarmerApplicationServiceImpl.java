package com.farmily.user.service.impl;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.dto.LoginRequest;
import com.farmily.user.dto.PublicFarmerResubmitRequest;
import com.farmily.user.exception.BusinessException;
import com.farmily.user.exception.DistrictNotFoundException;
import com.farmily.user.exception.ReviewNotFoundException;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.FarmerReviewRepository;
import com.farmily.user.service.EmailVerificationService;
import com.farmily.user.service.FarmerApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

// 讓狀態尚未啟用小農 (狀態 PENDING) 可以查進度
@Service
@Transactional
public class FarmerApplicationServiceImpl implements FarmerApplicationService {

    private final FarmerRepository farmerRepository;
    private final FarmerReviewRepository farmerReviewRepository;
    private final CityDistrictRepository cityDistrictRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public FarmerApplicationServiceImpl(FarmerRepository farmerRepository, FarmerReviewRepository farmerReviewRepository,
                                        CityDistrictRepository cityDistrictRepository, PasswordEncoder passwordEncoder,
                                        EmailVerificationService emailVerificationService) {
        this.farmerRepository = farmerRepository;
        this.farmerReviewRepository = farmerReviewRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    // 查自己最新審核結果 (狀態 + 退件理由)
    @Override
    @Transactional(readOnly = true)
    public FarmerReviewResponse checkStatus(LoginRequest log) {
        Farmer farmer = authByCredentials(log.getEmail(), log.getPassword());
        FarmerReview latest = farmerReviewRepository.findTopByFarmer_FarmerIdOrderByReviewRoundDesc(farmer.getFarmerId());

        // latest 防呆（active 小農理論上至少有一筆審核
        if (latest == null) {
            throw new ReviewNotFoundException();
        }
        FarmerReviewResponse res = FarmerReviewResponse.from(latest);
        res.maskAdminInfo();   // 小農端（含公開查詢進度）不能看到承辦管理員與內部備註

        // 小農端只看到「待審」：REVIEWING 對外顯示成 PENDING（只在這個小農呼叫點轉換，不動共用 from()）
        if ("REVIEWING".equals(res.getReviewStatus())) {
            res.setReviewStatus("PENDING");
        }

        // 帶上帳號狀態與 Email 驗證狀態：前端用來分辨「已核准但未驗證（需重寄啟用信）」vs「已可登入」
        res.setFarmerStatus(farmer.getFarmerStatus() != null ? farmer.getFarmerStatus().name() : null);
        res.setEmailVerified(farmer.getEmailVerified() != null && farmer.getEmailVerified());
        return res;
    }

    // 重寄「啟用信」：僅限已核准 (ACTIVE) 但尚未完成 Email 驗證的小農
    // 以 email + 密碼驗身（同查詢進度頁的驗證模型），故可給精準回饋
    @Override
    public void resendActivation(LoginRequest log) {
        Farmer farmer = authByCredentials(log.getEmail(), log.getPassword());

        // 409
        if (farmer.getEmailVerified() != null && farmer.getEmailVerified()) {
            throw new BusinessException("EMAIL_ALREADY_VERIFIED", HttpStatus.CONFLICT, "此帳號已完成 Email 驗證，請直接登入");
        }
        // 403
        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.ACTIVE) {
            throw new BusinessException("FARMER_NOT_APPROVED", HttpStatus.FORBIDDEN, "帳號尚未通過審核，審核通過後才會寄出啟用信");
        }
        emailVerificationService.sendFarmerActivation(farmer.getEmail());
    }

    // 未啟用小農重新送審（只允許小農狀態 PENDING + 審核狀態為 REJECTED 才能重新送審
    @Override
    public FarmerReviewResponse resubmit(PublicFarmerResubmitRequest req) {

        Farmer farmer = authByCredentials(req.getEmail(), req.getPassword());

        // 查最新審核
        FarmerReview latest = farmerReviewRepository.findTopByFarmer_FarmerIdOrderByReviewRoundDesc(farmer.getFarmerId());

        // 409
        if (farmer.getFarmerStatus() != Farmer.FarmerStatus.PENDING) {
            throw new BusinessException("FARMER_ALREADY_APPROVED", HttpStatus.CONFLICT, "此帳號已通過審核，請登入後操作");
        }

        // 僅最新一輪審核狀態為 REJECTED 才能重新送審；審核中 PENDING/REVIEWING 一律擋下 (409)
        if (latest == null || latest.getReviewStatus() != FarmerReview.ReviewStatus.REJECTED) {
            throw new BusinessException("FARMER_REVIEW_PENDING", HttpStatus.CONFLICT, "尚在審核中，需審核完成後才能重新送審");
        }

        // 審核狀態為 REJECTED:
        // 審核次數 + 1
        int nextRound = (latest != null && latest.getReviewRound() != null)
                ? latest.getReviewRound() + 1
                : 1;

        // 重新送審 = 重新寫入 FarmerReview
        FarmerReview review = new FarmerReview();
        review.setFarmer(farmer);
        review.setReviewStatus(FarmerReview.ReviewStatus.PENDING);
        review.setReviewRound(nextRound);
        review.setSubmittedAt(LocalDateTime.now());
        review.setSubmittedFarmName(req.getFarmName());
        review.setSubmittedFarmAddress(req.getFarmAddress());
        review.setSubmittedDistrict(findDistrict(req.getDistrictId()));
        review.setSubmittedLocLat(req.getLocLat());
        review.setSubmittedLocLong(req.getLocLong());
        review.setCertFileLand(toBytes(req.getCertFileLand()));
        review.setCertFileProduct(toBytes(req.getCertFileProduct()));
        review.setCertFileIdentity(toBytes(req.getCertFileIdentity()));

        return FarmerReviewResponse.from(farmerReviewRepository.save(review));
    }


    // 自定義方法: 用 email + password 驗證身分（取代登入入口與 session）
    private Farmer authByCredentials(String email, String password) {
        Farmer farmer = farmerRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        if (farmer.getPassword() == null
                || !passwordEncoder.matches(password, farmer.getPassword())) {
            throw new BadCredentialsException("帳號或密碼錯誤");
        }
        return farmer;
    }

    // 自定義方法: 上傳檔案 → byte[]（沒選檔回 null；讀取失敗轉 RuntimeException，同 ProductVO 做法）
    private byte[] toBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("證明文件讀取失敗", e);
        }
    }

    // 自定義方法: 依 id 撈區域
    private CityDistrict findDistrict(Integer districtId) {
        if (districtId == null) {
            return null;
        }
        return cityDistrictRepository.findById(districtId)
                .orElseThrow(() -> new DistrictNotFoundException());
//                .orElseThrow(DistrictNotFoundException::new);
    }

}
