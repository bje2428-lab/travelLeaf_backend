package dev.jpa.placestags;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlacesTagsRepository extends JpaRepository<PlacesTags, Long> {
    Optional<PlacesTags> findByTagName(String tagName);
}
