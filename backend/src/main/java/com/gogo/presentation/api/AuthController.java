package com.gogo.presentation.api;

import com.gogo.application.usecase.auth.KakaoLoginUseCase;
import com.gogo.application.usecase.auth.LogoutUseCase;
import com.gogo.application.usecase.auth.RefreshTokenUseCase;
import com.gogo.domain.entity.User;
import com.gogo.domain.repository.UserRepository;
import com.gogo.infrastructure.security.AuthenticatedUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final UserRepository userRepository;
    private final com.gogo.application.auth.KakaoOAuthClient kakaoOAuthClient;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public AuthController(KakaoLoginUseCase kakaoLoginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          UserRepository userRepository,
                          com.gogo.application.auth.KakaoOAuthClient kakaoOAuthClient) {
        this.kakaoLoginUseCase = kakaoLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.userRepository = userRepository;
        this.kakaoOAuthClient = kakaoOAuthClient;
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
            logoutUseCase.execute(principal.userId());
        }
        clearCookies(response);
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "인증이 필요합니다."));
        }
        Optional<User> user = userRepository.findById(principal.userId());
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        User u = user.get();
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "nickname", u.getNickname(),
                "profileImageUrl", u.getProfileImageUrl() != null ? u.getProfileImageUrl() : ""
        ));
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
