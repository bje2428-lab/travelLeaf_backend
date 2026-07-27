package dev.jpa.ai_weather;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AiWeatherRepository extends JpaRepository<AiWeather, Long> {

    /**
     * 최신 AI_REQUEST 기준
     * 일정 전체의 날씨 분석 결과 조회
     * - 일정 요약용
     */
    @Query("""
        select w
        from AiWeather w
        where w.schedule.scheduleId = :scheduleId
          and w.aiRequest.requestId = (
              select max(w2.aiRequest.requestId)
              from AiWeather w2
              where w2.schedule.scheduleId = :scheduleId
          )
        order by w.targetDate asc
    """)
    List<AiWeather> findLatestByScheduleId(
            @Param("scheduleId") Long scheduleId
    );

    /**
     * 최신 AI_REQUEST 기준
     * 특정 날짜의 날씨 분석 결과 전체 조회
     * (하루 여러 지점 대응)
     */
    @Query("""
        select w
        from AiWeather w
        where w.schedule.scheduleId = :scheduleId
          and w.targetDate = :targetDate
          and w.aiRequest.requestId = (
              select max(w2.aiRequest.requestId)
              from AiWeather w2
              where w2.schedule.scheduleId = :scheduleId
          )
        order by w.weatherType asc
    """)
    List<AiWeather> findLatestListByScheduleIdAndDate(
            @Param("scheduleId") Long scheduleId,
            @Param("targetDate") LocalDate targetDate
    );

    /* =====================================================
       ✅ 추가 1) detail 기준 조회 (핵심)
       - 지도/일정 상세 클릭 → 해당 지점 날씨
       - ⚠️ "해당 detailId에서 생성된 최신 분석(requestId)"만 가져와야 함
         (schedule 기준 최신으로 잡으면 다른 detail의 최신 분석과 섞일 수 있음)
    ===================================================== */
    @Query("""
        select w
        from AiWeather w
        where w.scheduleDetail.detailId = :detailId
          and w.aiRequest.requestId = (
              select max(w2.aiRequest.requestId)
              from AiWeather w2
              where w2.scheduleDetail.detailId = :detailId
          )
        order by w.targetDate asc
    """)
    List<AiWeather> findLatestByDetailId(
            @Param("detailId") Long detailId
    );

    /* =====================================================
       ✅ 추가 2) schedule + detail 기준 전체 조회
       - Day → Detail → Weather 완전 매핑용
       - 최신 분석 1회분(requestId max)만 가져오기
    ===================================================== */
    @Query("""
        select w
        from AiWeather w
        where w.schedule.scheduleId = :scheduleId
          and w.aiRequest.requestId = (
              select max(w2.aiRequest.requestId)
              from AiWeather w2
              where w2.schedule.scheduleId = :scheduleId
          )
        order by
            w.targetDate asc,
            w.scheduleDetail.dayNumber asc,
            w.scheduleDetail.orderInDay asc
    """)
    List<AiWeather> findLatestByScheduleIdOrderByDetail(
            @Param("scheduleId") Long scheduleId
    );

    /**
     * ✅ 일정 삭제 전 FK(ORA-02292) 방지를 위한 하위 데이터 삭제
     * - AI_WEATHER.SCHEDULE_ID FK가 NO ACTION 이라서 부모(SCHEDULE) 삭제 전에 반드시 지워야 함
     */
    @Modifying
    @Query("""
        delete from AiWeather w
        where w.schedule.scheduleId = :scheduleId
    """)
    int deleteByScheduleId(@Param("scheduleId") Long scheduleId);
}
