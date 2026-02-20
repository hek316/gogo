package com.gogo.presentation.api;

import com.gogo.application.dto.*;
import com.gogo.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {

    private final CreateGroupUseCase createGroupUseCase;
    private final JoinGroupUseCase joinGroupUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final SharePlaceToGroupUseCase sharePlaceToGroupUseCase;
    private final GetGroupPlacesUseCase getGroupPlacesUseCase;

    public GroupsController(CreateGroupUseCase createGroupUseCase,
                            JoinGroupUseCase joinGroupUseCase,
                            GetGroupUseCase getGroupUseCase,
                            SharePlaceToGroupUseCase sharePlaceToGroupUseCase,
                            GetGroupPlacesUseCase getGroupPlacesUseCase) {
        this.createGroupUseCase = createGroupUseCase;
        this.joinGroupUseCase = joinGroupUseCase;
        this.getGroupUseCase = getGroupUseCase;
        this.sharePlaceToGroupUseCase = sharePlaceToGroupUseCase;
        this.getGroupPlacesUseCase = getGroupPlacesUseCase;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createGroupUseCase.execute(request));
    }

    @PostMapping("/join")
    public ResponseEntity<GroupResponse> joinGroup(@Valid @RequestBody JoinGroupRequest request) {
        return ResponseEntity.ok(joinGroupUseCase.execute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long id) {
        return ResponseEntity.ok(getGroupUseCase.execute(id));
    }

    @PostMapping("/{id}/places")
    public ResponseEntity<GroupPlaceResponse> sharePlace(@PathVariable Long id,
                                                         @Valid @RequestBody SharePlaceRequest request) {
        SharePlaceRequest withGroupId = new SharePlaceRequest(id, request.placeId(), request.sharedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(sharePlaceToGroupUseCase.execute(withGroupId));
    }

    @GetMapping("/{id}/places")
    public ResponseEntity<List<GroupPlaceResponse>> getGroupPlaces(@PathVariable Long id) {
        return ResponseEntity.ok(getGroupPlacesUseCase.execute(id));
    }
}
