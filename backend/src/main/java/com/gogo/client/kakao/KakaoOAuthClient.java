package com.gogo.client.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class KakaoOAuthClient {

    private static final String AUTH_BASE = "https://kauth.kakao.com";
    private static final String API_BASE = "https://kapi.kakao.com";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient restClient;

    public KakaoOAuthClient(
            @Value("${oauth.kakao.client-id}") String clientId,
            @Value("${oauth.kakao.client-secret}") String clientSecret,
            @Value("${oauth.kakao.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.restClient = RestClient.create();
    }

    public String buildAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(AUTH_BASE + "/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .build()
                .toUriString();
    }

    public KakaoTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        return restClient.post()
                .uri(AUTH_BASE + "/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    @SuppressWarnings("unchecked")
    public KakaoUserInfo getUserInfo(String kakaoAccessToken) {
        Map<String, Object> response = restClient.get()
                .uri(API_BASE + "/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(Map.class);

        Long id = ((Number) response.get("id")).longValue();
        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) profile.get("nickname");
        String profileImageUrl = (String) profile.getOrDefault("profile_image_url", null);

        return new KakaoUserInfo(String.valueOf(id), nickname, profileImageUrl);
    }

    public record KakaoTokenResponse(
            String access_token,
            String token_type,
            String refresh_token,
            Integer expires_in,
            Integer refresh_token_expires_in
    ) {}

    public record KakaoUserInfo(String id, String nickname, String profileImageUrl) {}
}
