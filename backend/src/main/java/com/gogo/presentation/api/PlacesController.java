package com.gogo.presentation.api;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlacesController {

    private final AddPlaceUseCase addPlaceUseCase;
    private final GetPlacesUseCase getPlacesUseCase;
    private final GetPlaceUseCase getPlaceUseCase;
    private final DeletePlaceUseCase deletePlaceUseCase;
    private final MarkPlaceVisitedUseCase markPlaceVisitedUseCase;

    public PlacesController(AddPlaceUseCase addPlaceUseCase,
                            GetPlacesUseCase getPlacesUseCase,
                            GetPlaceUseCase getPlaceUseCase,
                            DeletePlaceUseCase deletePlaceUseCase,
                            MarkPlaceVisitedUseCase markPlaceVisitedUseCase) {
        this.addPlaceUseCase = addPlaceUseCase;
        this.getPlacesUseCase = getPlacesUseCase;
        this.getPlaceUseCase = getPlaceUseCase;
        this.deletePlaceUseCase = deletePlaceUseCase;
        this.markPlaceVisitedUseCase = markPlaceVisitedUseCase;
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> addPlace(@Valid @RequestBody AddPlaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addPlaceUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<PlaceResponse>> getPlaces(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(getPlacesUseCase.execute(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlace(@PathVariable Long id) {
        return ResponseEntity.ok(getPlaceUseCase.execute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        deletePlaceUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/visit")
    public ResponseEntity<PlaceResponse> markVisited(@PathVariable Long id) {
        return ResponseEntity.ok(markPlaceVisitedUseCase.execute(id));
    }
}
