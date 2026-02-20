package com.gogo.domain.repository;

import com.gogo.domain.entity.Meeting;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository {
    Meeting save(Meeting meeting);
    Optional<Meeting> findById(Long id);
    List<Meeting> findByGroupId(Long groupId);
    void deleteById(Long id);
}
