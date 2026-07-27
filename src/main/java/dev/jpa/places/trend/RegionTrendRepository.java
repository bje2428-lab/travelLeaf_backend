package dev.jpa.places.trend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionTrendRepository extends JpaRepository<RegionTrend, Long> {

    @Query(value = """
        SELECT
          rt.trend_id AS trendId,
          rt.region_id AS regionId,
          r.region_name AS regionName,
          rt.city_id AS cityId,
          c.city_name AS cityName,
          rt.score AS score,
          rt.rank_no AS rankNo,
          rta.subtitle AS subtitle,
          rta.keywords AS keywords,
          rta.hashtags AS hashtags,
          rta.one_line_reason AS oneLineReason
        FROM region_trend rt
        JOIN region r ON r.region_id = rt.region_id
        LEFT JOIN city c ON c.city_id = rt.city_id
        LEFT JOIN region_trend_ai rta ON rta.trend_id = rt.trend_id
        WHERE (rt.start_date, rt.end_date, rt.computed_at) IN (
          SELECT start_date, end_date, MAX(computed_at)
          FROM region_trend
          GROUP BY start_date, end_date
          ORDER BY MAX(computed_at) DESC
          FETCH FIRST 1 ROWS ONLY
        )
        ORDER BY rt.rank_no ASC
        FETCH FIRST :limit ROWS ONLY
        """, nativeQuery = true)
    List<TrendCardView> findLatestTopN(int limit);
}
