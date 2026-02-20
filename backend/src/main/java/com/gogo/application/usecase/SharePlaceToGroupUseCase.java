package com.gogo.application.usecase;

import com.gogo.application.dto.GroupPlaceResponse;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.dto.SharePlaceRequest;
import com.gogo.domain.entity.GroupPlace;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.GroupPlaceRepository;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SharePlaceToGroupUseCase {

    private final PlaceRepository placeRepository;
    private final GroupPlaceRepository groupPlaceRepository;

    public SharePlaceToGroupUseCase(PlaceRepository placeRepository, GroupPlaceRepository groupPlaceRepository) {
        this.placeRepository = placeRepository;
        this.groupPlaceRepository = groupPlaceRepository;
    }

    public GroupPlaceResponse execute(SharePlaceRequest request) {
        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + request.placeId()));
        GroupPlace groupPlace = GroupPlace.create(request.groupId(), request.placeId(), request.sharedBy());
        GroupPlace saved = groupPlaceRepository.save(groupPlace);
        return GroupPlaceResponse.of(saved, PlaceResponse.from(place));
    }
}
