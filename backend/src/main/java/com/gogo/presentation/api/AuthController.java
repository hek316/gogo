package com.gogo.presentation.api;

import com.gogo.application.auth.GoogleOAuthClient;
import com.gogo.application.auth.KakaoOAuthClient;
import com.gogo.application.service.AuthService;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int ACCESS_TOKEN_MAX_AGE = 15 * 60;           // 15 minutes
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final AuthService authService;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public AuthController(KakaoLoginUseCase kakaoLoginUseCase,
                          GoogleLoginUseCase googleLoginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          AuthService authService,
                          KakaoOAuthClient kakaoOAuthClient,
                          GoogleOAuthClient googleOAuthClient) {
        this.kakaoLoginUseCase = kakaoLoginUseCase;
        this.googleLoginUseCase = googleLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.authService = authService;
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.googleOAuthClient = googleOAuthClient;
    }

    @GetMapping("/kakao/authorize")
    public void authorize(HttpServletResponse response) throws Exception {
        response.sendRedirect(kakaoOAuthClient.buildAuthorizationUrl());
    }

    @GetMapping("/kakao/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) {
        handleOAuthCallback(code, response, kakaoLoginUseCase::execute);
    }

    @GetMapping("/google/authorize")
    public void googleAuthorize(HttpServletResponse response) throws Exception {
        response.sendRedirect(googleOAuthClient.buildAuthorizationUrl());
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam String code, HttpServletResponse response) {
        handleOAuthCallback(code, response, googleLoginUseCase::execute);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh-token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "refresh token이 없습니다."));
        }

        try {
            KakaoLoginUseCase.TokenPair tokens = refreshTokenUseCase.execute(refreshToken);
            addTokenCookie(response, "access-token", tokens.accessToken(), "/", ACCESS_TOKEN_MAX_AGE);
            addTokenCookie(response, "refresh-token", tokens.refreshToken(), "/api/auth", REFRESH_TOKEN_MAX_AGE);
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
            return ResponseEntity.ok(authService.getCurrentUser());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    private void handleOAuthCallback(String code, HttpServletResponse response,
                                     Function<String, KakaoLoginUseCase.TokenPair> loginFn) {
        try {
            KakaoLoginUseCase.TokenPair tokens = loginFn.apply(code);
            String redirectUrl = frontendUrl + "/auth/callback"
                    + "?at=" + tokens.accessToken()
                    + "&rt=" + tokens.refreshToken();
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            try {
                response.sendRedirect(frontendUrl + "/auth/error?message="
                        + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
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

    private void addTokenCookie(HttpServletResponse response, String name, String value,
                                String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void clearCookies(HttpServletResponse response) {
        addTokenCookie(response, "access-token", "", "/", 0);
        addTokenCookie(response, "refresh-token", "", "/api/auth", 0);
    }
}
