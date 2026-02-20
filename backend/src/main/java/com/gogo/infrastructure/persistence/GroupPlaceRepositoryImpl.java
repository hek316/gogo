package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.GroupPlace;
import com.gogo.domain.repository.GroupPlaceRepository;
import com.gogo.infrastructure.persistence.entity.GroupPlaceJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupPlaceRepositoryImpl implements GroupPlaceRepository {

    private final GroupPlaceJpaRepository groupPlaceJpaRepository;

    public GroupPlaceRepositoryImpl(GroupPlaceJpaRepository groupPlaceJpaRepository) {
        this.groupPlaceJpaRepository = groupPlaceJpaRepository;
    }

    @Override
    public GroupPlace save(GroupPlace groupPlace) {
        GroupPlaceJpaEntity entity = new GroupPlaceJpaEntity(
                groupPlace.getGroupId(), groupPlace.getPlaceId(),
                groupPlace.getSharedBy(), groupPlace.getSharedAt()
        );
        GroupPlaceJpaEntity saved = groupPlaceJpaRepository.save(entity);
        return GroupPlace.reconstruct(saved.getId(), saved.getGroupId(), saved.getPlaceId(),
                saved.getSharedBy(), saved.getSharedAt());
    }

    @Override
    public List<GroupPlace> findByGroupId(Long groupId) {
        return groupPlaceJpaRepository.findByGroupId(groupId).stream()
                .map(e -> GroupPlace.reconstruct(e.getId(), e.getGroupId(), e.getPlaceId(),
                        e.getSharedBy(), e.getSharedAt()))
                .toList();
    }

    @Override
    public boolean existsByGroupIdAndPlaceId(Long groupId, Long placeId) {
        return groupPlaceJpaRepository.existsByGroupIdAndPlaceId(groupId, placeId);
    }
}
