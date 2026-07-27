package dev.jpa.location;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/location")
public class LocationCont {

    private final RegionService regionService;
    private final CityService cityService;

    public LocationCont(RegionService regionService,
                              CityService cityService) {
        this.regionService = regionService;
        this.cityService = cityService;
    }

    // GET /location/regions
    @GetMapping("/regions")
    public List<RegionDTO> getRegions() {
        return regionService.getRegions();
    }

    // GET /location/cities?regionId=1
    @GetMapping("/cities")
    public List<CityDTO> getCities(@RequestParam("regionId") Long regionId) {
        return cityService.getCitiesByRegion(regionId);
    }
}
