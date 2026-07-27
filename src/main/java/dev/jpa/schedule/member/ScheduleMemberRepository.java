package dev.jpa.schedule.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleMemberRepository extends JpaRepository<ScheduleMember, ScheduleMemberId> {

    Optional<ScheduleMember> findByIdScheduleIdAndIdUserNo(Long scheduleId, Long userNo);

    boolean existsByIdScheduleIdAndIdUserNoAndStatus(Long scheduleId, Long userNo, String status);

    List<ScheduleMember> findByIdScheduleIdAndStatusOrderByJoinedAtAsc(Long scheduleId, String status);
}
