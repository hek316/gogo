package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.OAuthProvider;
import com.gogo.domain.entity.User;
import com.gogo.domain.repository.UserRepository;
import com.gogo.infrastructure.persistence.entity.UserJpaEntity;
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
        UserJpaEntity entity = UserJpaEntity.from(user);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByOauthIdAndProvider(String oauthId, OAuthProvider provider) {
        return jpaRepository.findByOauthIdAndProvider(oauthId, provider)
                .map(UserJpaEntity::toDomain);
    }
}
