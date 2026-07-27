package dev.jpa.bike;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import dev.jpa.bike.MapMarkerDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @GetMapping("/api/bike/nearby") // 특정 좌표 기준
    public List<MapMarkerDto> findNearby(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam(value = "radius", defaultValue = "3") double radius
    ) {
        return bikeService.findNearbyMarkers(lat, lng, radius);
    }
    
    @PostMapping("/api/bike/path/nearby") // 특정 코스 기준
    public List<MapMarkerDto> findNearbyByPath(
            @RequestBody PathSearchRequest request
    ) {
        return bikeService.findNearbyByPath(request);
    }
    
    @GetMapping("/api/bike/rentals/within")
    public List<MapMarkerDto> findRentalsWithin(
            @RequestParam("swLat") Double swLat,
            @RequestParam("swLng") Double swLng,
            @RequestParam("neLat") Double neLat,
            @RequestParam("neLng") Double neLng
    ) {
        return bikeService.findRentalsWithinBounds(
            swLat, swLng, neLat, neLng
        );
    }
    @GetMapping("/api/bike/repairs/within")
    public List<MapMarkerDto> findRepairsWithin(
            @RequestParam("swLat") Double swLat,
            @RequestParam("swLng") Double swLng,
            @RequestParam("neLat") Double neLat,
            @RequestParam("neLng") Double neLng
    ) {
        return bikeService.findRepairsWithinBounds(swLat, swLng, neLat, neLng);
    }



}
