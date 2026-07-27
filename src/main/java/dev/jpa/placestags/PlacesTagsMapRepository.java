package dev.jpa.placestags;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacesTagsMapRepository extends JpaRepository<PlacesTagsMap, PlacesTagsMap.Pk> {
    List<PlacesTagsMap> findByPlace_PlaceId(Long placeId);
    List<PlacesTagsMap> findByTag_TagName(String tagName);
}
