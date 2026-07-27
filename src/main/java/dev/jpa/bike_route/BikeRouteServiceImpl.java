package dev.jpa.bike_route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BikeRouteServiceImpl implements BikeRouteService {

    private final BikeRouteRepository bikeRouteRepository;
    private final BikeRoutePathRepository bikeRoutePathRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /* =========================
       1️⃣ 자전거길 목록 조회
       - Repository에서 BikeRoute 엔티티 목록 조회
       - hasPath 판단은 Service에서 수행
    ========================= */
    @Override
    public List<BikeRouteDTO.ListResponse> getRouteList() {

        List<BikeRoute> routes = bikeRouteRepository.findAllOrderByName();

        return routes.stream()
                .map(r -> new BikeRouteDTO.ListResponse(
                        r.getRouteId(),
                        r.getRouteName(),
                        r.getRegion(),
                        r.getCity(),
                        r.getTotalDistanceKm(),
                        r.getEstimatedTimeMin(),
                        bikeRoutePathRepository.existsByRouteId(r.getRouteId())
                ))
                .toList();
    }

    /* =========================
       2️⃣ 자전거길 상세 조회
       - Projection 사용
       - JSON 문자열 파싱
    ========================= */
    @Override
    public BikeRouteDTO.DetailResponse getRouteDetail(Long routeId) {

        BikeRouteProjection row = bikeRouteRepository.findRouteDetail(routeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 자전거길입니다. routeId=" + routeId)
                );

        return new BikeRouteDTO.DetailResponse(
                row.getRouteId(),
                row.getRouteName(),
                row.getRegion(),
                row.getCity(),
                row.getTotalDistanceKm(),
                row.getEstimatedTimeMin(),
                row.getDescription(),
                parseJsonArray(row.getHighlights()),
                parseJsonArray(row.getFoodInfo()),
                parseJsonArray(row.getTips()),
                bikeRoutePathRepository.existsByRouteId(routeId)
        );
    }

    /* =========================
       3️⃣ 자전거길 경로 좌표 조회
    ========================= */
    @Override
    public List<BikeRouteDTO.PathResponse> getRoutePath(Long routeId) {

        return bikeRoutePathRepository.findAllByRouteIdOrderBySeq(routeId)
                .stream()
                .map(p -> new BikeRouteDTO.PathResponse(
                        p.getSeq(),
                        p.getLat(),
                        p.getLng()
                ))
                .toList();
    }

    /* =========================
       JSON CLOB → List<String>
       - 파싱 실패 시 빈 리스트 반환
    ========================= */
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
