package dev.jpa.location;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    // ✅ 지역명으로 Region 조회 (AI_PLAN → BATCH → SCHEDULE 연결용)
    Optional<Region> findByRegionName(String regionName);
}