package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.OAuthProvider;
import com.gogo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthIdAndProvider(String oauthId, OAuthProvider provider);
}
