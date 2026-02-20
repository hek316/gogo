package com.gogo.infrastructure.persistence.entity;

import com.gogo.domain.entity.PlaceStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "places")
public class PlaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String address;

    @Column
    private String category;

    @Column
    private String url;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceStatus status;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PlaceJpaEntity() {}

    public PlaceJpaEntity(Long id, String name, String address, String category,
                          String url, String note, PlaceStatus status,
                          String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.category = category;
        this.url = url;
        this.note = note;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCategory() { return category; }
    public String getUrl() { return url; }
    public String getNote() { return note; }
    public PlaceStatus getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
