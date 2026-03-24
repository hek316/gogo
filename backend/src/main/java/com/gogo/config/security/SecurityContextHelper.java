package com.gogo.config.security;

import com.gogo.domain.model.AuthenticatedUser;
import com.gogo.domain.port.AuthContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContextHelper implements AuthContext {

    public Optional<Long> currentUserId() {
        return resolveUser().map(AuthenticatedUser::userId);
    }

    public Optional<String> currentNickname() {
        return resolveUser().map(AuthenticatedUser::nickname);
    }

    private Optional<AuthenticatedUser> resolveUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast);
    }
}
