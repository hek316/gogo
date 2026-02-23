package com.gogo.application.usecase.auth;

import com.gogo.domain.entity.User;
import com.gogo.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> execute(Long userId) {
        return userRepository.findById(userId);
    }
}
