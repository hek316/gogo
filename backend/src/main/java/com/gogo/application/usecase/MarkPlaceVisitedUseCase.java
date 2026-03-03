package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.application.port.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkPlaceVisitedUseCase {

    private final PlaceRepository placeRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final AuthContext authContext;

    public MarkPlaceVisitedUseCase(PlaceRepository placeRepository, PlaceLikeRepository placeLikeRepository, AuthContext authContext) {
        this.placeRepository = placeRepository;
        this.placeLikeRepository = placeLikeRepository;
        this.authContext = authContext;
    }

    public PlaceResponse execute(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        place.markAsVisited();
        Place saved = placeRepository.save(place);
        Long userId = authContext.currentUserId().orElse(null);
        return PlaceResponse.from(saved,
                placeLikeRepository.countByPlaceId(id),
                userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, id));
    }
}
