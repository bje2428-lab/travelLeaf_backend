package dev.jpa.bike_route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BikeRoutePathRepository extends JpaRepository<BikeRoutePath, Long> {

    /* =========================
       해당 routeId에 경로 존재 여부
    ========================= */
    boolean existsByRouteId(Long routeId);

    /* =========================
       경로 좌표 엔티티 조회
       - DTO 생성 ❌
       - 엔티티 그대로 반환
    ========================= */
    @Query("""
        SELECT p
        FROM BikeRoutePath p
        WHERE p.routeId = :routeId
        ORDER BY p.seq
    """)
    List<BikeRoutePath> findAllByRouteIdOrderBySeq(
        @Param("routeId") Long routeId);
}
