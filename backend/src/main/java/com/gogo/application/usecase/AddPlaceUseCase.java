package com.gogo.application.usecase;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.infrastructure.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddPlaceUseCase {

    private final PlaceRepository placeRepository;

    public AddPlaceUseCase(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public PlaceResponse execute(AddPlaceRequest request) {
        String nickname = extractNickname();
        Place place = Place.create(
                request.name(),
                request.address(),
                request.category(),
                request.url(),
                request.note(),
                request.imageUrl(),
                nickname
        );
        Place saved = placeRepository.save(place);
        return PlaceResponse.from(saved);
    }

    private String extractNickname() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user.nickname();
        }
        return "anonymous";
    }
}
