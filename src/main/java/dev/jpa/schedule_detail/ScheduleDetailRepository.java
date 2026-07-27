package dev.jpa.schedule_detail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleDetailRepository extends JpaRepository<ScheduleDetail, Long> {

    /**
     * 일정 전체 상세 조회
     * - Day 오름차순
     * - 하루 내 Order 오름차순
     */
    List<ScheduleDetail>
    findBySchedule_ScheduleIdOrderByDayNumberAscOrderInDayAsc(Long scheduleId);

    /**
     * 특정 Day 상세 조회
     */
    List<ScheduleDetail>
    findBySchedule_ScheduleIdAndDayNumberOrderByOrderInDayAsc(
            Long scheduleId,
            Integer dayNumber
    );

    /**
     * 특정 일정의 특정 Day 전체 삭제
     * - AI 일정 덮어쓰기 / Day 재생성용
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from ScheduleDetail d
        where d.schedule.scheduleId = :scheduleId
          and d.dayNumber = :dayNumber
    """)
    void deleteByScheduleIdAndDayNumber(
            @Param("scheduleId") Long scheduleId,
            @Param("dayNumber") Integer dayNumber
    );
}
