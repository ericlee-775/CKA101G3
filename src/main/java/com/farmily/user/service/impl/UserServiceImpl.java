package com.farmily.user.service.impl;

import com.farmily.user.dto.*;
import com.farmily.user.event.MemberRegisteredEvent;
import com.farmily.user.event.PasswordChangedEvent;
import com.farmily.user.exception.*;
import com.farmily.user.model.AccountToken;
import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.User;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.SpendingTierRepository;
import com.farmily.user.repository.UserRepository;

import com.farmily.user.service.EmailUniquenessChecker;
import com.farmily.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CityDistrictRepository cityDistrictRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailUniquenessChecker emailUniquenessChecker;
    private final SpendingTierRepository spendingTierRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository,
                           CityDistrictRepository cityDistrictRepository,
                           PasswordEncoder passwordEncoder,
                           EmailUniquenessChecker emailUniquenessChecker,
                           SpendingTierRepository spendingTierRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.cityDistrictRepository = cityDistrictRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailUniquenessChecker = emailUniquenessChecker;
        this.spendingTierRepository = spendingTierRepository;
        this.eventPublisher = eventPublisher;
    }

    // 本地註冊流程
    @Override
    public UserProfileResponse register(UserRegisterRequest reg) {

        // step1: 先檢查是否存在相同會員帳號 (email)
        User existingUser = userRepository.findByEmail(reg.getEmail()).orElse(null);    //回傳 Optional

        // 會員帳號 (email) 存在
        if (existingUser != null) {

            // 狀況 A: 帳號「沒有本地密碼」，代表他只有第三方登入資訊
            if (existingUser.getPassword() == null
                    && existingUser.getAuthProvider() == User.AuthProvider.GOOGLE) {
                throw new OAuthAccountConflictException();
            }
            // 狀況 B: 剩餘(已有本地密碼)就是一般重複註冊
            throw new EmailAlreadyExistsException();
        }

        // step2: 若 email = null，跨表檢查 email 全域唯一
        emailUniquenessChecker.emailAvailable(reg.getEmail());

        // step3: 會員帳號 (email) 不存在，走本地註冊流程
        User newUser = new User();
        newUser.setEmail(reg.getEmail());

        // hash 原始密碼
        String hashedPassword = passwordEncoder.encode(reg.getPassword());
        newUser.setPassword(hashedPassword);

        newUser.setAuthProvider(User.AuthProvider.LOCAL);
        newUser.setUserName(reg.getUserName());
        newUser.setUserNickname(reg.getUserNickname());

        // 抓 city 物件前先判斷
        if (reg.getDistrictId() != null) {
            CityDistrict city = cityDistrictRepository.findById(reg.getDistrictId())
                    .orElseThrow(() -> new DistrictNotFoundException());
//                    .orElseThrow(DistrictNotFoundException::new);
            newUser.setCityDistrict(city);
        }

        newUser.setUserAddress(reg.getUserAddress());
        newUser.setUserPhoneNum(reg.getUserPhoneNum());
        newUser.setBirthday(reg.getBirthday());
        newUser.setUserCreatedAt(LocalDateTime.now());
        newUser.setEmailVerified(false);
        newUser.setMonthlySpending(BigDecimal.ZERO);
        newUser.setUserStatus(User.UserStatus.ACTIVE);

        /*
        存進 DB (注意需用 saveAndFlush，不是 save)
        一般 save()，INSERT 不會馬上送到 DB，而是等「交易 commit」才送
        commit 發生在 register() 回傳之後，例外會在方法外面才爆 - 接不到
        */
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(newUser);   // 強迫立即送到 DB，萬一撞 unique 約束，例外當場丟出來，還可以接住
        } catch (DataIntegrityViolationException e) {
            // 能進到這裡 = 另一個並發請求在「我查完」到「我存下去」之間搶先註冊了同一 email
            // 被 DB 的 unique 約束擋下，轉成自訂業務例外 - 回 409
            throw new EmailAlreadyExistsException();
        }

        // 寄出 Email 驗證信 + 產生 Redis token
//        emailVerificationService.sendVerification(
//                savedUser.getEmail(), AccountToken.AccountType.MEMBER);
        // 用事件監聽器確保交易成功 commit 後才執行寄信事件
        eventPublisher.publishEvent(
                new MemberRegisteredEvent(savedUser.getEmail(), AccountToken.AccountType.MEMBER));

        // 包裝會員資料成 dto 給 Controller
        return UserProfileResponse.from(savedUser);
    }

    // 本地登入流程
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse login(LoginRequest log) {
        User user = userRepository.findByEmail(log.getEmail())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 純 Google 帳號沒有本地密碼，null 檢查必須在 matches() 之前
        // LoginRequest 有密碼非空值驗證，可移除
//        if (user.getPassword() == null) {
//            throw new IllegalStateException("此帳號為第三方登入，請改用 Google 登入");
//        }

        // 檢查 hash 密碼是否相等
        if (!passwordEncoder.matches(log.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("帳號或密碼錯誤");
        }

        if (user.getUserStatus() == User.UserStatus.SUSPENDED
                || user.getUserStatus() == User.UserStatus.DELETED) {
            throw new AccountSuspendedException();
        }

        // 本地帳號必須先完成 Email 驗證（點驗證信連結）才能登入；Google OAuth 除外
        if (user.getAuthProvider() == User.AuthProvider.LOCAL
                && (user.getEmailVerified() == null || !user.getEmailVerified())) {
            throw new EmailNotVerifiedException();
        }
        return UserProfileResponse.from(user);
    }

    // 查資料
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
//                .orElseThrow(UserNotFoundException::new);

        // +消費級距 (不同表)
        BigDecimal amount = user.getMonthlySpending() != null ? user.getMonthlySpending() : BigDecimal.ZERO;
        String tierName = spendingTierRepository.findTierNameByAmount(amount);

        return UserProfileResponse.from(user, tierName);
    }

    // 修改資料
    @Override
    public UserProfileResponse updateMyProfile(Integer userId, UserUpdateRequest update) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
//                .orElseThrow(UserNotFoundException::new);


        if (update.getUserName() != null)
            user.setUserName(update.getUserName());
        if (update.getUserNickname() != null)
            user.setUserNickname(update.getUserNickname());
        if (update.getUserPhoneNum() != null)
            user.setUserPhoneNum(update.getUserPhoneNum());
        if (update.getUserAddress() != null)
            user.setUserAddress(update.getUserAddress());
        if (update.getBirthday() != null)
            user.setBirthday(update.getBirthday());
        if (update.getDistrictId() != null) {
            CityDistrict city = cityDistrictRepository.findById(update.getDistrictId())
                    .orElseThrow(() -> new DistrictNotFoundException());
//                    .orElseThrow(DistrictNotFoundException::new);
            user.setCityDistrict(city);
        }
        //  將修改資料存進 DB
        return UserProfileResponse.from(userRepository.save(user));
    }

    // 修改密碼
    @Override
    public void changePassword(Integer userId, ChangePasswordRequest pw) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
//                .orElseThrow(UserNotFoundException::new);


        // 已有本地密碼，必須先驗證舊密碼正確
        if (user.getPassword() != null) {
            if (pw.getOldPassword() == null
                    || !passwordEncoder.matches(pw.getOldPassword(), user.getPassword())) {
                throw new BadCredentialsException("舊密碼錯誤");
            }
        }

        // Google 帳號首次設定本地密碼和本地帳號新密碼一樣：本來就沒有 oldPassword，直接 hash 新密碼存入
        user.setPassword(passwordEncoder.encode(pw.getNewPassword()));
        userRepository.save(user);

        // 用事件監聽器確保密碼變更成功(交易成功 commit)後寄通知信
        eventPublisher.publishEvent(new PasswordChangedEvent(user.getEmail()));
    }

    // 刪除資料
    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }
        userRepository.deleteById(userId);
    }

    // OAuth 2.0 註冊登入
    @Override
    public UserProfileResponse loginOrRegisterOAuth(OAuthUserInfo info) {
        // step1. 先用 Google 驗證可信 info 的 providerId 找會員
        User user = userRepository.findByProviderId(info.getProviderId()).orElse(null);

        // step2. 用 providerId 找不到，改用 email 找
        if(user == null){
            user = userRepository.findByEmail(info.getEmail()).orElse(null);

            // 若 email 存在
            if(user != null){
                // 則此會員有本地帳號+密碼，多榜定 Google 回傳的 provider_id
                user.setProviderId(info.getProviderId());

                // 有本地帳號一定有密碼
//                if(user.getPassword() == null){
//                    user.setAuthProvider(User.AuthProvider.GOOGLE);
//                }
            }
        }
        // step3. id、email 都找不到，進入註冊會員流程
        if(user == null) {
            user = new User();
            user.setEmail(info.getEmail());
            user.setUserName(info.getName());
            user.setPassword(null);
            user.setAuthProvider(User.AuthProvider.GOOGLE);
            user.setProviderId(info.getProviderId());
            user.setEmailVerified(true);
            user.setUserCreatedAt(LocalDateTime.now());
            user.setMonthlySpending(BigDecimal.ZERO);
            user.setUserStatus(User.UserStatus.ACTIVE);
        }

        // step4. 限制被停權、註銷帳號不能登入
        if (user.getUserStatus() == User.UserStatus.SUSPENDED
                || user.getUserStatus() == User.UserStatus.DELETED) {
            throw new AccountSuspendedException();
        }

        // 包裝會員資料成 dto 給 Controller
        return UserProfileResponse.from(userRepository.save(user));
    }
}

