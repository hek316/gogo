package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.OAuthProvider;
import com.gogo.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByOauthIdAndProvider(String oauthId, OAuthProvider provider);
}
