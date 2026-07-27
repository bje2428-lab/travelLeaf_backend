// src/main/java/dev/jpa/places/Places.java
package dev.jpa.places;

import dev.jpa.location.City;
import dev.jpa.location.Region;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PLACES")
public class Places {

    @Id
    @Column(name = "place_id")
    private Long placeId; // ✅ 카카오 id를 그대로 저장(직접 세팅)

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "source_type", length = 50)
    private String sourceType; // 선택: "KAKAO"

    @Column(name = "source_id", length = 200)
    private String sourceId; // 선택: 굳이 안 써도 됨 (유니크 걸지 말 것 추천)

    @Column(name = "is_active", length = 1)
    private String isActive; // Y / N

    @Column(name = "difficulty", length = 10)
    private String difficulty;

    @Column(name = "distance_km")
    private Double distanceKM;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Places() {}

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isActive == null) this.isActive = "Y";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getter/Setter =====
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getIsActive() { return isActive; }
    public void setIsActive(String isActive) { this.isActive = isActive; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Double getDistanceKM() { return distanceKM; }
    public void setDistanceKM(Double distanceKM) { this.distanceKM = distanceKM; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
