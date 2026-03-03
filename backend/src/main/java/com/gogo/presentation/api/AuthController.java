package com.gogo.presentation.api;

import com.gogo.application.auth.GoogleOAuthClient;
import com.gogo.application.auth.KakaoOAuthClient;
import com.gogo.application.service.AuthService;
import com.gogo.application.usecase.GetCurrentUserUseCase;
import com.gogo.application.usecase.auth.GoogleLoginUseCase;
import com.gogo.application.usecase.auth.KakaoLoginUseCase;
import com.gogo.application.usecase.auth.RefreshTokenUseCase;
import com.gogo.infrastructure.security.AuthenticatedUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final AuthService authService;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public AuthController(KakaoLoginUseCase kakaoLoginUseCase,
                          GoogleLoginUseCase googleLoginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          AuthService authService,
                          GetCurrentUserUseCase getCurrentUserUseCase,
                          KakaoOAuthClient kakaoOAuthClient,
                          GoogleOAuthClient googleOAuthClient) {
        this.kakaoLoginUseCase = kakaoLoginUseCase;
        this.googleLoginUseCase = googleLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.authService = authService;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.googleOAuthClient = googleOAuthClient;
    }

    @GetMapping("/kakao/authorize")
    public void authorize(HttpServletResponse response) throws Exception {
        String redirectUrl = kakaoOAuthClient.buildAuthorizationUrl();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/kakao/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws Exception {
        try {
            KakaoLoginUseCase.TokenPair tokens = kakaoLoginUseCase.execute(code);
            String redirectUrl = frontendUrl + "/auth/callback"
                    + "?at=" + tokens.accessToken()
                    + "&rt=" + tokens.refreshToken();
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/auth/error?message=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/google/authorize")
    public void googleAuthorize(HttpServletResponse response) throws Exception {
        String redirectUrl = googleOAuthClient.buildAuthorizationUrl();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam String code, HttpServletResponse response) throws Exception {
        try {
            KakaoLoginUseCase.TokenPair tokens = googleLoginUseCase.execute(code);
            String redirectUrl = frontendUrl + "/auth/callback"
                    + "?at=" + tokens.accessToken()
                    + "&rt=" + tokens.refreshToken();
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/auth/error?message=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh-token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "refresh token이 없습니다."));
        }

        try {
            KakaoLoginUseCase.TokenPair tokens = refreshTokenUseCase.execute(refreshToken);
            setAccessTokenCookie(response, tokens.accessToken());
            setRefreshTokenCookie(response, tokens.refreshToken());
            return ResponseEntity.ok(Map.of("message", "토큰이 갱신되었습니다."));
        } catch (IllegalArgumentException e) {
            clearCookies(response);
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal AuthenticatedUser principal,
                                    HttpServletResponse response) {
        if (principal != null) {
            authService.logout(principal.userId());
        }
        clearCookies(response);
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        try {
            return ResponseEntity.ok(getCurrentUserUseCase.execute());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(15 * 60); // 15 minutes
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }

    private void clearCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("access-token", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh-token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}
