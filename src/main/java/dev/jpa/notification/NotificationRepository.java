package dev.jpa.notification;

import dev.jpa.admin.dto.NotificationAdminDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ✅ 발송 대기(PENDING 등) 페이징 조회
    Page<Notification> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);

    // ✅ 관리자 목록 (USER_ID 문자열 포함)
    @Query("""
        select new dev.jpa.admin.dto.NotificationAdminDto(
            n.notiId,
            n.userId,
            coalesce(u.userid, '-'),
            n.toEmail,
            n.channel,
            n.type,
            n.title,
            n.status,
            n.createdAt,
            n.sentAt,
            n.failReason
        )
        from Notification n
        left join dev.jpa.user.User u on u.userno = n.userId
        where
          (:q is null or :q = '' or
            lower(coalesce(u.userid, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(n.toEmail, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(n.title, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(n.type, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(n.status, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(n.channel, '')) like lower(concat('%', :q, '%'))
          )
          and (:from is null or n.createdAt >= :from)
          and (:to is null or n.createdAt < :to)
        order by n.createdAt desc
    """)
    List<NotificationAdminDto> findAdminList(
            @Param("q") String q,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
