package com.farmily.user.util;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 「記住我」用的 JSESSIONID cookie 工具。
 *
 * 本專案是 session 登入，瀏覽器只靠 JSESSIONID cookie 當鑰匙。Tomcat 預設發的是
 * session cookie（沒有 Max-Age，關掉瀏覽器就被刪）。勾了「記住我」時，這裡再補寫一個
 * 帶 Max-Age 的同名 cookie 去覆蓋它，讓它寫進硬碟、關瀏覽器後仍保留。
*/

@Component
public class SessionCookieSupport {

    /** 勾「記住我」：server session 與 persistent cookie 的壽命（天），兩者一致 */
    private static final int REMEMBER_ME_DAYS = 14;
    /** 不勾「記住我」：server session 閒置多久沒操作就過期（分鐘） */
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    // 從設定檔注入；找不到時給合理預設值（本機 http：secure=false、SameSite=Lax）
    private final boolean secure;
    private final String sameSite;

    public SessionCookieSupport(
            @Value("${server.servlet.session.cookie.secure:false}") boolean secure,
            @Value("${server.servlet.session.cookie.same-site:Lax}") String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    /*
     * 依「記住我」設定 server session 壽命，並決定 JSESSIONID cookie 要不要寫進硬碟。
     *  勾了：server session 14 天，並補寫帶 Max-Age 的 persistent cookie（關瀏覽器仍保留）
     *  沒勾：server session 30 分鐘，沿用 Tomcat 預設的 session cookie（關瀏覽器即刪）
     * 兩個 controller（會員 / 小農）登入共用此方法，避免重複
     */
    public void applyRememberMe(HttpSession session, HttpServletResponse response, boolean rememberMe) {
        if (rememberMe) {
            session.setMaxInactiveInterval((int) Duration.ofDays(REMEMBER_ME_DAYS).getSeconds());
            writeRememberMeCookie(session, response);
        } else {
            session.setMaxInactiveInterval((int) Duration.ofMinutes(DEFAULT_TIMEOUT_MINUTES).getSeconds());
        }
    }

    /**
     * 補寫一個帶 Max-Age 的 persistent JSESSIONID cookie。必須在 getSession(true) 之後呼叫
     * 這個 Set-Cookie 會排在 Tomcat 預設那個 session cookie 之後，瀏覽器採用後者變成持久化
     * cookie 屬性與 application.properties 的全域設定一致（HttpOnly / Secure / SameSite）
     */
    private void writeRememberMeCookie(HttpSession session, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", session.getId())
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(Duration.ofDays(REMEMBER_ME_DAYS))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
