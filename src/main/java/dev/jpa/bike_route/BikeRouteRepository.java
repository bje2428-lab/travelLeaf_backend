package dev.jpa.bike_route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BikeRouteRepository extends JpaRepository<BikeRoute, Long> {

    /* =========================
       1️⃣ 자전거길 목록 조회
       - BIKE_ROUTE 테이블만 조회
       - hasPath 판단 ❌ (Service에서 처리)
    ========================= */
    @Query("""
        SELECT r
        FROM BikeRoute r
        ORDER BY r.routeName
    """)
    List<BikeRoute> findAllOrderByName();

    /* =========================
       2️⃣ 자전거길 상세 조회
       - Projection 사용 (CLOB 포함)
    ========================= */
    @Query("""
        SELECT r.routeId AS routeId,
               r.routeName AS routeName,
               r.region AS region,
               r.city AS city,
               r.totalDistanceKm AS totalDistanceKm,
               r.estimatedTimeMin AS estimatedTimeMin,
               r.description AS description,
               r.highlights AS highlights,
               r.foodInfo AS foodInfo,
               r.tips AS tips
        FROM BikeRoute r
        WHERE r.routeId = :routeId
    """)
    Optional<BikeRouteProjection> findRouteDetail(
        @Param("routeId") Long routeId);
}
