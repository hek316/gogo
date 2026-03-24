package com.gogo.db.repository;

import com.gogo.db.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByGroupId(Long groupId);
}
