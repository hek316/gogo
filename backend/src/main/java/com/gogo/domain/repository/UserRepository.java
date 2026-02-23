package com.gogo.domain.repository;

import com.gogo.domain.entity.OAuthProvider;
import com.gogo.domain.entity.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByKakaoIdAndProvider(String kakaoId, OAuthProvider provider);
}
