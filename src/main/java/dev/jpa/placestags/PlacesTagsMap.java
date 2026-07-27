package dev.jpa.placestags;

import dev.jpa.places.Places;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "PLACES_TAGS_MAP")
public class PlacesTagsMap {

    @EmbeddedId
    private Pk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("placeId")
    @JoinColumn(name = "place_id")
    private Places place;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private PlacesTags tag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PlacesTagsMap() {}

    public PlacesTagsMap(Places place, PlacesTags tag) {
        this.place = place;
        this.tag = tag;
        this.id = new Pk(place.getPlaceId(), tag.getTagId());
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Pk getId() { return id; }
    public Places getPlace() { return place; }
    public PlacesTags getTag() { return tag; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Embeddable
    public static class Pk implements Serializable {
        private Long placeId;
        private Long tagId;

        public Pk() {}

        public Pk(Long placeId, Long tagId) {
            this.placeId = placeId;
            this.tagId = tagId;
        }

        public Long getPlaceId() { return placeId; }
        public Long getTagId() { return tagId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk)) return false;
            Pk pk = (Pk) o;
            return Objects.equals(placeId, pk.placeId) && Objects.equals(tagId, pk.tagId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(placeId, tagId);
        }
    }
}
