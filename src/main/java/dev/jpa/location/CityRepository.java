package dev.jpa.location;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    // 기존: Region ID로 도시 목록 조회 (유지)
    List<City> findByRegionRegionId(Long regionId);

    // ✅ 추가: 지역 + 도시명으로 단건 조회 (AI 일정 저장용)
    Optional<City> findByCityNameAndRegion(String cityName, Region region);
}
