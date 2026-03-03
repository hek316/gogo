package com.gogo.application.service;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlaceCommandService {

    private final PlaceRepository placeRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final AuthContext authContext;

    public PlaceCommandService(PlaceRepository placeRepository,
                               PlaceLikeRepository placeLikeRepository,
                               AuthContext authContext) {
        this.placeRepository = placeRepository;
        this.placeLikeRepository = placeLikeRepository;
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

    public PlaceResponse markVisited(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        place.markAsVisited();
        Place saved = placeRepository.save(place);
        Long userId = authContext.currentUserId().orElse(null);
        return PlaceResponse.from(saved,
                placeLikeRepository.countByPlaceId(id),
                userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, id));
    }

    public void deletePlace(Long id) {
        placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        placeRepository.deleteById(id);
    }
}
