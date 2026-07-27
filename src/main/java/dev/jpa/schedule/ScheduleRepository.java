package dev.jpa.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.user.User;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /* =========================
       공유 / 코드 조회
    ========================= */

    // 공유 코드로 최신 일정 1건
    Optional<Schedule> findTopByShareCodeOrderByScheduleIdDesc(String shareCode);

    boolean existsByShareCode(String shareCode);

    // 내부 일정 코드 조회
    Optional<Schedule> findByScheduleCode(String scheduleCode);

    /* =========================
       단건 / 목록 (fetch join)
    ========================= */

    @Query("""
        select s from Schedule s
        join fetch s.user u
        left join fetch s.region r
        left join fetch s.city c
        left join fetch s.startRegion sr
        left join fetch s.startCity sc
        left join fetch s.endRegion er
        left join fetch s.endCity ec
        where s.scheduleId = :scheduleId
    """)
    Optional<Schedule> findByIdWithJoins(@Param("scheduleId") Long scheduleId);

    @Query("""
        select distinct s from Schedule s
        join fetch s.user u
        left join fetch s.region r
        left join fetch s.city c
        left join fetch s.startRegion sr
        left join fetch s.startCity sc
        left join fetch s.endRegion er
        left join fetch s.endCity ec
        where u.userno = :userNo
           or exists (
              select 1 from ScheduleMember m
              where m.schedule = s
                and m.id.userNo = :userNo
                and m.status = 'ACTIVE'
           )
        order by s.createdAt desc
    """)
    List<Schedule> findAllMineOrJoinedWithJoins(@Param("userNo") Long userNo);

    /* =========================
       기본 조회
    ========================= */

    List<Schedule> findByUser(User user);

    List<Schedule> findByUserAndIsPublic(User user, String isPublic);

    List<Schedule> findByUser_UsernoOrderByScheduleIdDesc(Long userNo);
}
