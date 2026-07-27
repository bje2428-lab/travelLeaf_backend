package dev.jpa.placestags;

import dev.jpa.places.Places;
import dev.jpa.places.PlacesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlacesTagsService {

    private final PlacesRepository placesRepository;
    private final PlacesTagsRepository tagsRepository;
    private final PlacesTagsMapRepository mapRepository;

    public PlacesTagsService(PlacesRepository placesRepository,
                             PlacesTagsRepository tagsRepository,
                             PlacesTagsMapRepository mapRepository) {
        this.placesRepository = placesRepository;
        this.tagsRepository = tagsRepository;
        this.mapRepository = mapRepository;
    }

    public PlacesTagsDTO.ListResponse save(PlacesTagsDTO.SaveRequest req) {

        Places place = placesRepository.findById(req.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("Place not found. placeId=" + req.getPlaceId()));

        List<String> normalized = req.getTags().stream()
                .map(t -> t == null ? "" : t.trim())
                .filter(t -> !t.isBlank())
                .map(t -> t.startsWith("#") ? t.substring(1) : t)
                .distinct()
                .collect(Collectors.toList());

        for (String tagName : normalized) {
            PlacesTags tag = tagsRepository.findByTagName(tagName)
                    .orElseGet(() -> {
                        PlacesTags newTag = new PlacesTags();
                        newTag.setTagName(tagName);
                        return tagsRepository.save(newTag);
                    });

            PlacesTagsMap.Pk pk = new PlacesTagsMap.Pk(place.getPlaceId(), tag.getTagId());
            if (!mapRepository.existsById(pk)) {
                mapRepository.save(new PlacesTagsMap(place, tag));
            }
        }

        return list(place.getPlaceId());
    }

    @Transactional(readOnly = true)
    public PlacesTagsDTO.ListResponse list(Long placeId) {
        List<String> tags = mapRepository.findByPlace_PlaceId(placeId).stream()
                .map(m -> "#" + m.getTag().getTagName())
                .distinct()
                .collect(Collectors.toList());

        PlacesTagsDTO.ListResponse res = new PlacesTagsDTO.ListResponse();
        res.setPlaceId(placeId);
        res.setTags(tags);
        return res;
    }

    // ✅ 핵심: sourceId 없으면 200 + 빈 배열
    @Transactional(readOnly = true)
    public PlacesTagsDTO.ListResponse getTagsBySourceId(String sourceId) {

        Optional<Places> optPlace = placesRepository.findBySourceId(sourceId);

        if (optPlace.isEmpty()) {
            PlacesTagsDTO.ListResponse res = new PlacesTagsDTO.ListResponse();
            res.setPlaceId(null);
            res.setTags(Collections.emptyList());
            return res;
        }

        return list(optPlace.get().getPlaceId());
    }
}
