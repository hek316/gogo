package com.gogo.application.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class GoogleOAuthClient {

    private static final String AUTH_BASE = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient restClient;

    public GoogleOAuthClient(
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret,
            @Value("${oauth.google.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.restClient = RestClient.create();
    }

    public String buildAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(AUTH_BASE)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile")
                .build()
                .toUriString();
    }

    public GoogleTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        return restClient.post()
                .uri(TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    @SuppressWarnings("unchecked")
    public GoogleUserInfo getUserInfo(String accessToken) {
        Map<String, Object> response = restClient.get()
                .uri(USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String sub = (String) response.get("sub");
        String name = (String) response.getOrDefault("name", "");
        String picture = (String) response.getOrDefault("picture", null);

        return new GoogleUserInfo(sub, name, picture);
    }

    public record GoogleTokenResponse(
            String access_token,
            String token_type,
            String id_token,
            Integer expires_in
    ) {}

    public record GoogleUserInfo(String id, String nickname, String profileImageUrl) {}
}
