package dev.jpa.bike_route;

import java.util.List;

public interface BikeRouteService {

    // 자전거길 목록
    List<BikeRouteDTO.ListResponse> getRouteList();

    // 자전거길 상세
    BikeRouteDTO.DetailResponse getRouteDetail(Long routeId);

    // 자전거길 경로 좌표
    List<BikeRouteDTO.PathResponse> getRoutePath(Long routeId);
}
