// src/main/java/dev/jpa/places/PlacesRepository.java
package dev.jpa.places;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlacesRepository extends JpaRepository<Places, Long> {
    Optional<Places> findBySourceId(String sourceId);
}
