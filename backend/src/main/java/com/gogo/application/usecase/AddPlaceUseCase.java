package com.gogo.application.usecase;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.application.port.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddPlaceUseCase {

    private final PlaceRepository placeRepository;
    private final AuthContext authContext;

    public AddPlaceUseCase(PlaceRepository placeRepository, AuthContext authContext) {
        this.placeRepository = placeRepository;
        this.authContext = authContext;
    }

    public PlaceResponse execute(AddPlaceRequest request) {
        String nickname = authContext.currentNickname().orElse("anonymous");
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
