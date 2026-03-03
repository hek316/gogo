package com.gogo.application.service;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlaceCommandService {

    private final PlaceRepository placeRepository;
    private final AuthContext authContext;

    public PlaceCommandService(PlaceRepository placeRepository, AuthContext authContext) {
        this.placeRepository = placeRepository;
        this.authContext = authContext;
    }

    public PlaceResponse addPlace(AddPlaceRequest request) {
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

    public void deletePlace(Long id) {
        placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        placeRepository.deleteById(id);
    }
}
