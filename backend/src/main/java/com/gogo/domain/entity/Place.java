package com.gogo.domain.entity;

import java.time.LocalDateTime;

public class Place {

    private Long id;
    private String name;
    private String address;
    private String category;
    private String url;
    private String note;
    private String imageUrl;
    private PlaceStatus status;
    private String createdBy;
    private LocalDateTime createdAt;

    private Place() {}

    public static Place create(String name, String address, String category, String url, String note, String imageUrl, String createdBy) {
        validate(name);
        Place place = new Place();
        place.name = name;
        place.address = address;
        place.category = category;
        place.url = url;
        place.note = note;
        place.imageUrl = imageUrl;
        place.status = PlaceStatus.WANT_TO_GO;
        place.createdBy = createdBy;
        place.createdAt = LocalDateTime.now();
        return place;
    }

    public static Place reconstruct(Long id, String name, String address, String category,
                                    String url, String note, String imageUrl, PlaceStatus status,
                                    String createdBy, LocalDateTime createdAt) {
        Place place = new Place();
        place.id = id;
        place.name = name;
        place.address = address;
        place.category = category;
        place.url = url;
        place.note = note;
        place.imageUrl = imageUrl;
        place.status = status;
        place.createdBy = createdBy;
        place.createdAt = createdAt;
        return place;
    }

    public void markAsVisited() {
        this.status = PlaceStatus.VISITED;
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("장소 이름은 필수입니다.");
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCategory() { return category; }
    public String getUrl() { return url; }
    public String getNote() { return note; }
    public String getImageUrl() { return imageUrl; }
    public PlaceStatus getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
