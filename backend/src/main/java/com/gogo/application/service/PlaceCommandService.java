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
    private final PlaceQueryService placeQueryService;
    private final AuthContext authContext;

    public PlaceCommandService(PlaceRepository placeRepository,
                               PlaceQueryService placeQueryService,
                               AuthContext authContext) {
        this.placeRepository = placeRepository;
        this.placeQueryService = placeQueryService;
        this.authContext = authContext;
    }

    public PlaceResponse addPlace(AddPlaceRequest request) {
        String nickname = authContext.requireNickname();
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

    public PlaceResponse markVisited(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        place.markAsVisited();
        placeRepository.save(place);
        return placeQueryService.toResponse(place);
    }

    public void deletePlace(Long id) {
        if (!placeRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id);
        }
        placeRepository.deleteById(id);
    }
}
