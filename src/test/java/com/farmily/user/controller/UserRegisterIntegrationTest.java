package com.farmily.user.controller;

import com.farmily.user.model.User;
import com.farmily.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest                 // 啟動「整個」Spring context (真 bean、真 DB、真 Redis)
@AutoConfigureMockMvc           // 準備 MockMvc，讓我們能打真的 API 路徑
@Transactional
public class UserRegisterIntegrationTest {

    @Autowired
    MockMvc mockMvc;                 // 用來發 HTTP 請求

    @Autowired
    UserRepository userRepository;   // 真的 repository，查真 DB

    @Autowired
    PasswordEncoder passwordEncoder; // 真的 BCrypt

    @MockitoBean
    JavaMailSender mailSender;      // mock 掉，不要真的寄信

    @Test
    @DisplayName("整合測試：POST /api/member/register 真的把會員存進 DB，且密碼經 BCrypt 雜湊")
    void registerIntegration() throws Exception {
        String json = "{ \"email\": \"integration@example.com\","
                + " \"password\": \"test12345\","
                + " \"userName\": \"整合測試\" }";

        // 打真的 API，預期回 201 Created
        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // 直接查真 DB 驗證資料真的進去了
        User saved = userRepository.findByEmail("integration@example.com").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("test12345");                               // 斷言不是明碼
        assertThat(passwordEncoder.matches("test12345", saved.getPassword())).isTrue();     // 斷言密碼有加密
        assertThat(saved.getEmailVerified()).isFalse();                                               // 斷言信箱未驗證
    }
}