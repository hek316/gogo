package com.gogo.db.repository;

import com.gogo.db.entity.OAuthProvider;
import com.gogo.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthIdAndProvider(String oauthId, OAuthProvider provider);
}
