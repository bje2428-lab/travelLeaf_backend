package dev.jpa.bike_route;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bike_routes")
@RequiredArgsConstructor
public class BikeRouteCont {

    private final BikeRouteService bikeRouteService;

    /* =========================
       1️⃣ 자전거길 목록
       GET /bike-routes
    ========================= */
    @GetMapping
    public List<BikeRouteDTO.ListResponse> getRoutes() {
        return bikeRouteService.getRouteList();
    }

    /* =========================
       2️⃣ 자전거길 상세
       GET /bike-routes/{routeId}
    ========================= */
    @GetMapping("/{routeId}")
    public BikeRouteDTO.DetailResponse getRouteDetail(
            @PathVariable("routeId") Long routeId
    ) {
        return bikeRouteService.getRouteDetail(routeId);
    }

    /* =========================
       3️⃣ 자전거길 경로 좌표
       GET /bike-routes/{routeId}/path
    ========================= */
    @GetMapping("/{routeId}/path")
    public List<BikeRouteDTO.PathResponse> getRoutePath(
            @PathVariable("routeId") Long routeId
    ) {
        return bikeRouteService.getRoutePath(routeId);
    }
}
