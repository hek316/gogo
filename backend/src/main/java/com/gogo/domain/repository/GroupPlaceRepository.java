package com.gogo.domain.repository;

import com.gogo.domain.entity.GroupPlace;

import java.util.List;

public interface GroupPlaceRepository {
    GroupPlace save(GroupPlace groupPlace);
    List<GroupPlace> findByGroupId(Long groupId);
    boolean existsByGroupIdAndPlaceId(Long groupId, Long placeId);
}
