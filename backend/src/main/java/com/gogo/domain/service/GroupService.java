package com.gogo.domain.service;

import com.gogo.domain.dto.*;
import com.gogo.domain.port.AuthContext;
import com.gogo.db.entity.Group;
import com.gogo.db.entity.GroupPlace;
import com.gogo.db.entity.Place;
import com.gogo.db.repository.GroupPlaceRepository;
import com.gogo.db.repository.GroupRepository;
import com.gogo.db.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupPlaceRepository groupPlaceRepository;
    private final PlaceRepository placeRepository;
    private final AuthContext authContext;

    public GroupService(GroupRepository groupRepository,
                        GroupPlaceRepository groupPlaceRepository,
                        PlaceRepository placeRepository,
                        AuthContext authContext) {
        this.groupRepository = groupRepository;
        this.groupPlaceRepository = groupPlaceRepository;
        this.placeRepository = placeRepository;
        this.authContext = authContext;
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long id) {
        return groupRepository.findById(id)
                .map(GroupResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다. id=" + id));
    }

    public GroupResponse createGroup(CreateGroupRequest request) {
        String nickname = authContext.requireNickname();
        Group group = Group.create(request.name(), nickname);
        return GroupResponse.from(groupRepository.save(group));
    }

    public GroupResponse joinGroup(JoinGroupRequest request) {
        String nickname = authContext.requireNickname();
        Group group = groupRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));
        group.addMember(nickname);
        return GroupResponse.from(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<GroupPlaceResponse> getGroupPlaces(Long groupId) {
        return groupPlaceRepository.findByGroupId(groupId).stream()
                .map(gp -> placeRepository.findById(gp.getPlaceId())
                        .map(place -> GroupPlaceResponse.of(gp, PlaceResponse.from(place)))
                        .orElse(null))
                .filter(r -> r != null)
                .toList();
    }

    public GroupPlaceResponse sharePlaceToGroup(SharePlaceRequest request) {
        String sharedBy = authContext.requireNickname();
        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + request.placeId()));
        GroupPlace groupPlace = GroupPlace.create(request.groupId(), request.placeId(), sharedBy);
        GroupPlace saved = groupPlaceRepository.save(groupPlace);
        return GroupPlaceResponse.of(saved, PlaceResponse.from(place));
    }
}
