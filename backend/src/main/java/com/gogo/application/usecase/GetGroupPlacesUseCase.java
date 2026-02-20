package com.gogo.application.usecase;

import com.gogo.application.dto.GroupPlaceResponse;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.GroupPlace;
import com.gogo.domain.repository.GroupPlaceRepository;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetGroupPlacesUseCase {

    private final GroupPlaceRepository groupPlaceRepository;
    private final PlaceRepository placeRepository;

    public GetGroupPlacesUseCase(GroupPlaceRepository groupPlaceRepository, PlaceRepository placeRepository) {
        this.groupPlaceRepository = groupPlaceRepository;
        this.placeRepository = placeRepository;
    }

    public List<GroupPlaceResponse> execute(Long groupId) {
        return groupPlaceRepository.findByGroupId(groupId).stream()
                .map(gp -> placeRepository.findById(gp.getPlaceId())
                        .map(place -> GroupPlaceResponse.of(gp, PlaceResponse.from(place)))
                        .orElse(null))
                .filter(r -> r != null)
                .toList();
    }
}
