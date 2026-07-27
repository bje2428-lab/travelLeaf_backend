package dev.jpa.location;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    public List<RegionDTO> getRegions() {
        return regionRepository.findAll()
                .stream()
                .map(RegionDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
