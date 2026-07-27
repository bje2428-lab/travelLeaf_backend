package dev.jpa.schedule.share;

import dev.jpa.admin.dto.ScheduleShareLogAdminDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleShareLogRepository extends JpaRepository<ScheduleShareLog, Long> {

    // =========================
    // ✅ (관리자) 스케줄 공유 기록 목록
    // GET /admin/schedule-share-logs?q=&date=
    // =========================
    @Query("""
        select new dev.jpa.admin.dto.ScheduleShareLogAdminDto(
            l.shareId,
            s.scheduleId,
            s.scheduleTitle,
            u.userid,
            l.channel,
            l.target,
            l.status,
            l.errorMsg,
            l.createdAt,
            l.sentAt
        )
        from ScheduleShareLog l
        join l.schedule s
        join s.user u
        where
            (:q is null or :q = '' or
                lower(u.userid) like lower(concat('%', :q, '%')) or
                lower(coalesce(s.scheduleTitle, '')) like lower(concat('%', :q, '%')) or
                lower(coalesce(l.channel, '')) like lower(concat('%', :q, '%')) or
                lower(coalesce(l.target, '')) like lower(concat('%', :q, '%')) or
                lower(coalesce(l.status, '')) like lower(concat('%', :q, '%')) or
                lower(function('to_char', s.scheduleId)) like lower(concat('%', :q, '%'))
            )
            and (:from is null or l.createdAt >= :from)
            and (:to is null or l.createdAt < :to)
        order by l.createdAt desc
    """)
    List<ScheduleShareLogAdminDto> findAdminList(
            @Param("q") String q,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // =========================
    // ✅ (일반 사용자) 특정 스케줄의 공유 로그 목록
    // GET /api/schedule/{scheduleId}/share-logs?userNo=19
    //
    // ✅ ScheduleShareCont.java 에서 호출하는 바로 그 메서드
    // =========================
    List<ScheduleShareLog> findBySchedule_ScheduleIdOrderByCreatedAtDesc(Long scheduleId);
}
