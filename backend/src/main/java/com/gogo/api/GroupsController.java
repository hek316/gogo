package com.gogo.api;

import com.gogo.domain.dto.*;
import com.gogo.domain.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {

    private final GroupService groupService;

    public GroupsController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request));
    }

    @PostMapping("/join")
    public ResponseEntity<GroupResponse> joinGroup(@Valid @RequestBody JoinGroupRequest request) {
        return ResponseEntity.ok(groupService.joinGroup(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroup(id));
    }

    @PostMapping("/{id}/places")
    public ResponseEntity<GroupPlaceResponse> sharePlace(@PathVariable Long id,
                                                         @Valid @RequestBody SharePlaceRequest request) {
        SharePlaceRequest withGroupId = new SharePlaceRequest(id, request.placeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.sharePlaceToGroup(withGroupId));
    }

    @GetMapping("/{id}/places")
    public ResponseEntity<List<GroupPlaceResponse>> getGroupPlaces(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupPlaces(id));
    }
}
