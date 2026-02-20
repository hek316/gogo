package com.gogo.domain.repository;

import com.gogo.domain.entity.Group;

import java.util.Optional;

public interface GroupRepository {
    Group save(Group group);
    Optional<Group> findById(Long id);
    Optional<Group> findByInviteCode(String inviteCode);
    void deleteById(Long id);
}
