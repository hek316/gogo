package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.application.port.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetPlacesUseCase {

    private final PlaceRepository placeRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final AuthContext authContext;

    public GetPlacesUseCase(PlaceRepository placeRepository, PlaceLikeRepository placeLikeRepository, AuthContext authContext) {
        this.placeRepository = placeRepository;
        this.placeLikeRepository = placeLikeRepository;
        this.authContext = authContext;
    }

    public List<PlaceResponse> execute(String category) {
        Long userId = authContext.currentUserId().orElse(null);
        List<com.gogo.domain.entity.Place> places = (category != null && !category.isBlank())
                ? placeRepository.findByCategory(category)
                : placeRepository.findAll();
        return places.stream()
                .map(p -> PlaceResponse.from(p,
                        placeLikeRepository.countByPlaceId(p.getId()),
                        userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, p.getId())))
                .toList();
    }
}
