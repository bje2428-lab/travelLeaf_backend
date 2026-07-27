// src/main/java/dev/jpa/places/PlacesDescriptionRepository.java
package dev.jpa.places;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacesDescriptionRepository
        extends JpaRepository<PlacesDescription, Long> {
}
