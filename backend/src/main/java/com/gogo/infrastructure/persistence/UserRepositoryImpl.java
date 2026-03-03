package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.OAuthProvider;
import com.gogo.domain.entity.User;
import com.gogo.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByOauthIdAndProvider(String oauthId, OAuthProvider provider) {
        return jpaRepository.findByOauthIdAndProvider(oauthId, provider);
    }
}
