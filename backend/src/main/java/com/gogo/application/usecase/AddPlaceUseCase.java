package com.gogo.application.usecase;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddPlaceUseCase {

    private final PlaceRepository placeRepository;
    private final SecurityContextHelper securityContextHelper;

    public AddPlaceUseCase(PlaceRepository placeRepository, SecurityContextHelper securityContextHelper) {
        this.placeRepository = placeRepository;
        this.securityContextHelper = securityContextHelper;
    }

    public PlaceResponse execute(AddPlaceRequest request) {
        String nickname = securityContextHelper.currentNickname().orElse("anonymous");
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
}
