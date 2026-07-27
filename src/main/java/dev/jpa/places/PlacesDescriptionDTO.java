package dev.jpa.places;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

public class PlacesDescriptionDTO {

    @Getter @Setter
    public static class UpsertRequest {
        private Long placeId;
        private String description;
        private String imageUrl;
        private String tags;
        private String moodKeywords;
    }

    @Getter @Setter
    public static class Response {
        private Long placeId;
        private String description;
        private String imageUrl;
        private String tags;
        private String moodKeywords;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(PlacesDescription e) {
            Response r = new Response();
            r.setPlaceId(e.getPlaceId());
            r.setDescription(e.getDescription());
            r.setImageUrl(e.getImageUrl());
            r.setTags(e.getTags());
            r.setMoodKeywords(e.getMoodKeywords());
            r.setCreatedAt(e.getCreatedAt());
            r.setUpdatedAt(e.getUpdatedAt());
            return r;
        }
    }
}
