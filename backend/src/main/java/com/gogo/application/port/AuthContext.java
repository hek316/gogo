package com.gogo.application.port;

import java.util.Optional;

public interface AuthContext {
    Optional<Long> currentUserId();
    Optional<String> currentNickname();
}
