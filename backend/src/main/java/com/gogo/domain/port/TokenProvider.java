package com.gogo.domain.port;

public interface TokenProvider {
    String generateAccessToken(Long userId, String nickname);
}
