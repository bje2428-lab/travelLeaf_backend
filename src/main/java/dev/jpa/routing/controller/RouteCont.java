package dev.jpa.routing.controller;

import dev.jpa.routing.dto.BikeDistanceReq;
import dev.jpa.routing.dto.BikeDistanceRes;
import dev.jpa.routing.service.OrsRouteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route")
public class RouteCont {

    private final OrsRouteService orsRouteService;

    public RouteCont(OrsRouteService orsRouteService) {
        this.orsRouteService = orsRouteService;
    }

    @PostMapping("/bike-distance")
    public BikeDistanceRes bikeDistance(@RequestBody BikeDistanceReq req) {
        double meters = orsRouteService.getBikeDistanceMeters(req);
        return new BikeDistanceRes(meters);
    }
}
