package com.gogo.application.usecase;

import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.User;
import com.gogo.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final AuthContext authContext;

    public GetCurrentUserUseCase(UserRepository userRepository, AuthContext authContext) {
        this.userRepository = userRepository;
        this.authContext = authContext;
    }

    public Map<String, Object> execute() {
        Long userId = authContext.currentUserId()
                .orElseThrow(() -> new IllegalStateException("인증이 필요합니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return Map.of(
                "id", user.getId(),
                "nickname", user.getNickname(),
                "profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : ""
        );
    }
}
