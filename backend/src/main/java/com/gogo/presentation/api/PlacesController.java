package com.gogo.presentation.api;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlacePreviewResponse;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.dto.PlaceSearchResult;
import com.gogo.application.service.PlaceCommandService;
import com.gogo.application.usecase.*;
import com.gogo.application.usecase.LikePlaceUseCase;
import com.gogo.application.usecase.UnlikePlaceUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlacesController {
    private static final Logger log = LoggerFactory.getLogger(PlacesController.class);

    private final AddPlaceUseCase addPlaceUseCase;
    private final GetPlacesUseCase getPlacesUseCase;
    private final GetPlaceUseCase getPlaceUseCase;
    private final PlaceCommandService placeCommandService;
    private final MarkPlaceVisitedUseCase markPlaceVisitedUseCase;
    private final GetPopularPlacesUseCase getPopularPlacesUseCase;
    private final GetRecentPlacesUseCase getRecentPlacesUseCase;
    private final FetchPlacePreviewUseCase fetchPlacePreviewUseCase;
    private final SearchPlacesUseCase searchPlacesUseCase;
    private final LikePlaceUseCase likePlaceUseCase;
    private final UnlikePlaceUseCase unlikePlaceUseCase;

    public PlacesController(AddPlaceUseCase addPlaceUseCase,
                            GetPlacesUseCase getPlacesUseCase,
                            GetPlaceUseCase getPlaceUseCase,
                            PlaceCommandService placeCommandService,
                            MarkPlaceVisitedUseCase markPlaceVisitedUseCase,
                            GetPopularPlacesUseCase getPopularPlacesUseCase,
                            GetRecentPlacesUseCase getRecentPlacesUseCase,
                            FetchPlacePreviewUseCase fetchPlacePreviewUseCase,
                            SearchPlacesUseCase searchPlacesUseCase,
                            LikePlaceUseCase likePlaceUseCase,
                            UnlikePlaceUseCase unlikePlaceUseCase) {
        this.addPlaceUseCase = addPlaceUseCase;
        this.getPlacesUseCase = getPlacesUseCase;
        this.getPlaceUseCase = getPlaceUseCase;
        this.placeCommandService = placeCommandService;
        this.markPlaceVisitedUseCase = markPlaceVisitedUseCase;
        this.getPopularPlacesUseCase = getPopularPlacesUseCase;
        this.getRecentPlacesUseCase = getRecentPlacesUseCase;
        this.fetchPlacePreviewUseCase = fetchPlacePreviewUseCase;
        this.searchPlacesUseCase = searchPlacesUseCase;
        this.likePlaceUseCase = likePlaceUseCase;
        this.unlikePlaceUseCase = unlikePlaceUseCase;
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
        placeCommandService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/visit")
    public ResponseEntity<PlaceResponse> markVisited(@PathVariable Long id) {
        return ResponseEntity.ok(markPlaceVisitedUseCase.execute(id));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PlaceResponse>> getPopularPlaces(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(getPopularPlacesUseCase.execute(limit));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PlaceResponse>> getRecentPlaces(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(getRecentPlacesUseCase.execute(limit));
    }

    @GetMapping("/preview")
    public ResponseEntity<PlacePreviewResponse> previewPlace(@RequestParam String url) {
        return ResponseEntity.ok(fetchPlacePreviewUseCase.execute(url));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlaceSearchResult>> searchPlaces(@RequestParam String keyword) {
        log.info("GET /api/places/search called. keyword='{}'", keyword);
        return ResponseEntity.ok(searchPlacesUseCase.execute(keyword));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePlace(@PathVariable Long id) {
        likePlaceUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePlace(@PathVariable Long id) {
        unlikePlaceUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
