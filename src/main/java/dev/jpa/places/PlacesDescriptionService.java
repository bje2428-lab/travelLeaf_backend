package dev.jpa.places;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlacesDescriptionService {

    private final PlacesRepository placesRepository;
    private final PlacesDescriptionRepository descRepository;

    public PlacesDescriptionService(PlacesRepository placesRepository,
                                    PlacesDescriptionRepository descRepository) {
        this.placesRepository = placesRepository;
        this.descRepository = descRepository;
    }

    public PlacesDescriptionDTO.Response upsert(PlacesDescriptionDTO.UpsertRequest req) {

        Places place = placesRepository.findById(req.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("Place not found. placeId=" + req.getPlaceId()));

        PlacesDescription desc = descRepository.findById(req.getPlaceId())
                .orElseGet(PlacesDescription::new);

        desc.setPlace(place);
        desc.setDescription(req.getDescription());
        desc.setImageUrl(req.getImageUrl());
        desc.setTags(req.getTags());
        desc.setMoodKeywords(req.getMoodKeywords());

        PlacesDescription saved = descRepository.save(desc);
        return PlacesDescriptionDTO.Response.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PlacesDescriptionDTO.Response get(Long placeId) {
        return descRepository.findById(placeId)
                .map(PlacesDescriptionDTO.Response::fromEntity)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public PlacesDescriptionDTO.Response getBySourceId(String sourceId) {
        // ✅ Places(sourceId) 가 우리 DB에 없으면 -> null 반환(200)
        Optional<Places> optPlace = placesRepository.findBySourceId(sourceId);
        if (optPlace.isEmpty()) return null;

        Long placeId = optPlace.get().getPlaceId();

        return descRepository.findById(placeId)
                .map(PlacesDescriptionDTO.Response::fromEntity)
                .orElse(null);
    }

    public void delete(Long placeId) {
        descRepository.deleteById(placeId);
    }
}
