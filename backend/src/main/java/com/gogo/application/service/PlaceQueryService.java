package com.gogo.application.service;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlaceQueryService {

    private final PlaceRepository placeRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final AuthContext authContext;

    public PlaceQueryService(PlaceRepository placeRepository,
                             PlaceLikeRepository placeLikeRepository,
                             AuthContext authContext) {
        this.placeRepository = placeRepository;
        this.placeLikeRepository = placeLikeRepository;
        this.authContext = authContext;
    }

    public PlaceResponse getPlace(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        Long userId = authContext.currentUserId().orElse(null);
        return PlaceResponse.from(place,
                placeLikeRepository.countByPlaceId(id),
                userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, id));
    }

    public List<PlaceResponse> getPlaces(String category) {
        Long userId = authContext.currentUserId().orElse(null);
        List<Place> places = (category != null && !category.isBlank())
                ? placeRepository.findByCategory(category)
                : placeRepository.findAll();
        return places.stream()
                .map(p -> PlaceResponse.from(p,
                        placeLikeRepository.countByPlaceId(p.getId()),
                        userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, p.getId())))
                .toList();
    }

    public List<PlaceResponse> getPopularPlaces(int limit) {
        Long userId = authContext.currentUserId().orElse(null);
        return placeRepository.findPopularPlaces(limit).stream()
                .map(p -> PlaceResponse.from(p,
                        placeLikeRepository.countByPlaceId(p.getId()),
                        userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, p.getId())))
                .toList();
    }

    public List<PlaceResponse> getRecent(int limit) {
        Long userId = authContext.currentUserId().orElse(null);
        return placeRepository.findRecent(limit).stream()
                .map(p -> PlaceResponse.from(p,
                        placeLikeRepository.countByPlaceId(p.getId()),
                        userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, p.getId())))
                .toList();
    }
}
