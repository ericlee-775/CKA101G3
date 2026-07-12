package com.farmily.user.service.impl;

import com.farmily.user.dto.UserRegisterRequest;
import com.farmily.user.event.MemberRegisteredEvent;
import com.farmily.user.exception.EmailAlreadyExistsException;
import com.farmily.user.exception.OAuthAccountConflictException;
import com.farmily.user.model.User;
import com.farmily.user.repository.CityDistrictRepository;
import com.farmily.user.repository.SpendingTierRepository;
import com.farmily.user.repository.UserRepository;
import com.farmily.user.service.EmailService;
import com.farmily.user.service.EmailUniquenessChecker;
import com.farmily.user.service.EmailVerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)          // 啟用 Mockito
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CityDistrictRepository cityDistrictRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailUniquenessChecker emailUniquenessChecker;

    @Mock
    private SpendingTierRepository spendingTierRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    // 把 7 個假替身塞進 UserServiceImpl
    @InjectMocks
    private UserServiceImpl userService;

    @DisplayName("單元測試-會員重複註冊")
    @Test
    void register() {
        UserRegisterRequest reg = new UserRegisterRequest();
        reg.setEmail("test@example.com");
        reg.setPassword("test12345");
        reg.setUserName("測試");

        // 模擬這個 email 已經有一個「有本地密碼」的帳號
        User existingUser = new User();
        existingUser.setPassword("hashed");

        // Mockito - 當使用 findByEmail 方法，請回答 existingUser 物件
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.register(reg);
        });

        // Mockito - 驗證 userRepository 的 save 從來沒被呼叫過 (重複註冊)
        verify(userRepository, never()).save(any());
    }

    @DisplayName("單元測試-Google 帳號重複註冊")
    @Test
    void registerGoogleAccountConflict() {
        UserRegisterRequest reg = new UserRegisterRequest();
        reg.setEmail("test@example.com");
        reg.setPassword("test12345");
        reg.setUserName("測試");

        // ====== 劇本 ======
        // 模擬這個 email 是 Google 帳號（沒有本地密碼）
        User googleUser = new User();
        googleUser.setPassword(null);
        googleUser.setAuthProvider(User.AuthProvider.GOOGLE);

        // Mockito - 當使用 findByEmail 方法，就固定返回 googleUser 物件
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(googleUser));

        // ====== 演戲 ======
        assertThrows(OAuthAccountConflictException.class, () -> {
            userService.register(reg);
        });

        // ====== 假做 ======
        // Mockito - 驗證不該去存 DB (重複註冊)
        verify(userRepository, never()).save(any());
    }

    // 測試全新 email 註冊成功：密碼雜湊、未驗證、寄驗證信
    @DisplayName("單元測試-會員註冊成功")
    @Test
    void registerNewEmailSuccess() {
        UserRegisterRequest reg = new UserRegisterRequest();
        reg.setEmail("test@example.com");
        reg.setPassword("test12345");
        reg.setUserName("測試");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("test12345")).thenReturn("HASHED");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));   // 把傳進來的參數原封不動回傳

        userService.register(reg);

        // Mockito - 驗證有呼叫密碼加密流程
        verify(passwordEncoder).encode("test12345");

        // ArgumentCaptor 參數捕獲器
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        // 驗證有寫入資料庫流程 + 側錄器:把準備傳進 save() 的那個 User 物件在半路攔截下來
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();                     // 拿出被攔截的物件

        assertEquals(saved.getPassword(),"HASHED");   // 斷言為加密過
        assertFalse(saved.getEmailVerified());              // 斷言信箱未驗證
        assertEquals(saved.getAuthProvider(), User.AuthProvider.LOCAL); // 斷言是本地註冊

        // Mockito - 驗證有觸發寄信通知流程
        verify(eventPublisher).publishEvent(any(MemberRegisteredEvent.class));
    }

    // 測試並發競態：saveAndFlush 撞 DB unique 約束拋自訂例外 EmailAlreadyExistsException
    @DisplayName("單元測試-註冊並發競態邏輯正確")
    @Test
    void registerRaceConditionHitsDbConstraint() {
        UserRegisterRequest reg = new UserRegisterRequest();
        reg.setEmail("test@example.com");
        reg.setPassword("test12345");
        reg.setUserName("測試");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("test12345")).thenReturn("HASHED");

        // 並發模擬 - 存入資料庫（saveAndFlush）瞬間，因別人先存，所以 DB 噴出 duplicate 錯誤
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.register(reg);
        });
    }


}