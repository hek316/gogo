package com.gogo.db.repository;

import com.gogo.db.entity.GroupPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupPlaceRepository extends JpaRepository<GroupPlace, Long> {
    List<GroupPlace> findByGroupId(Long groupId);
    boolean existsByGroupIdAndPlaceId(Long groupId, Long placeId);
}
