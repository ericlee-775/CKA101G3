package com.farmily.user.service.impl;

import com.farmily.user.dto.UserRegisterRequest;
import com.farmily.user.exception.EmailAlreadyExistsException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)          // 啟用 Mockito
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CityDistrictRepository cityDistrictRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    EmailUniquenessChecker emailUniquenessChecker;

    @Mock
    SpendingTierRepository spendingTierRepository;

    @Mock
    EmailVerificationService emailVerificationService;

    @Mock
    EmailService emailService;

    // 把 7 個假替身塞進 UserServiceImpl
    @InjectMocks
    UserServiceImpl userService;

    // 共用測試資料：一個合法註冊請求
    private UserRegisterRequest sampleRequest() {
        UserRegisterRequest reg = new UserRegisterRequest();
        reg.setEmail("test@example.com");
        reg.setPassword("test12345");
        reg.setUserName("測試");
        return reg;
    }

    @DisplayName("測試會員重複註冊")
    @Transactional
    @Test
    void register() {
        // Arrange（安排）：假裝這個 email 已經有一個「有本地密碼」的帳號
        User existingUser = new User();
        existingUser.setPassword("hashed");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));

        // assertThrows：第一個參數=預期的例外型別，第二個=會丟例外的那段程式
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.register(sampleRequest());
        });

        // 驗證 userRepository 的 save 從來沒被呼叫過
        verify(userRepository, never()).save(any());
    }
}