package dev.jpa.location;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<CityDTO> getCitiesByRegion(Long regionId) {
        return cityRepository.findByRegionRegionId(regionId)
                .stream()
                .map(CityDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
