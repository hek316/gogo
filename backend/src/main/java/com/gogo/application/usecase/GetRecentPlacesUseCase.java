package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetRecentPlacesUseCase {

    private final PlaceRepository placeRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final SecurityContextHelper securityContextHelper;

    public GetRecentPlacesUseCase(PlaceRepository placeRepository, PlaceLikeRepository placeLikeRepository, SecurityContextHelper securityContextHelper) {
        this.placeRepository = placeRepository;
        this.placeLikeRepository = placeLikeRepository;
        this.securityContextHelper = securityContextHelper;
    }

    public List<PlaceResponse> execute(int limit) {
        Long userId = securityContextHelper.currentUserId().orElse(null);
        return placeRepository.findRecent(limit).stream()
                .map(p -> PlaceResponse.from(p,
                        placeLikeRepository.countByPlaceId(p.getId()),
                        userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, p.getId())))
                .toList();
    }
}
