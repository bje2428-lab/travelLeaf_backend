package dev.jpa.places;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PLACES_DESCRIPTION")
public class PlacesDescription {

    @Id
    @Column(name = "place_id")
    private Long placeId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "place_id")
    private Places place;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "mood_keywords", length = 500)
    private String moodKeywords;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PlacesDescription() {}

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getter/Setter =====
    public Long getPlaceId() { return placeId; }

    public Places getPlace() { return place; }
    public void setPlace(Places place) { this.place = place; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getMoodKeywords() { return moodKeywords; }
    public void setMoodKeywords(String moodKeywords) { this.moodKeywords = moodKeywords; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
