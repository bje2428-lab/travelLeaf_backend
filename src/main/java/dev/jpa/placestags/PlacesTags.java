package dev.jpa.placestags;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PLACES_TAGS")
public class PlacesTags {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "places_tags_seq")
    @SequenceGenerator(name = "places_tags_seq", sequenceName = "SEQ_PLACES_TAGS", allocationSize = 1)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", length = 50, nullable = false, unique = true)
    private String tagName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PlacesTags() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getTagId() { return tagId; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
