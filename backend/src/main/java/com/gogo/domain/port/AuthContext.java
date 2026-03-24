package com.gogo.domain.port;

import java.util.Optional;

public interface AuthContext {
    Optional<Long> currentUserId();
    Optional<String> currentNickname();

    default Long requireUserId() {
        return currentUserId()
                .orElseThrow(() -> new IllegalStateException("인증 정보가 없습니다."));
    }

    default String requireNickname() {
        return currentNickname().orElse("anonymous");
    }
}
