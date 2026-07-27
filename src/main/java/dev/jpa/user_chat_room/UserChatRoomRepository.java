package dev.jpa.user_chat_room;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    /** 내 채팅방 목록 */
    @Query("""
        SELECT r FROM UserChatRoom r
        WHERE r.userANo = :userNo OR r.userBNo = :userNo
    """)
    List<UserChatRoom> findByUserNo(@Param("userNo") Long userNo);

    /** 두 유저의 1:1 방 찾기(양방향) */
    @Query("""
        SELECT r FROM UserChatRoom r
        WHERE (r.userANo = :a AND r.userBNo = :b)
           OR (r.userANo = :b AND r.userBNo = :a)
    """)
    Optional<UserChatRoom> findByUsers(@Param("a") Long userA, @Param("b") Long userB);

    /**
     * ✅ 내가 방을 열었을 때 읽은 시간 갱신
     * - userNo가 A면 LAST_READ_A_AT 업데이트
     * - userNo가 B면 LAST_READ_B_AT 업데이트
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE UserChatRoom r
        SET
          r.lastReadAAt = CASE WHEN r.userANo = :userNo THEN :readAt ELSE r.lastReadAAt END,
          r.lastReadBAt = CASE WHEN r.userBNo = :userNo THEN :readAt ELSE r.lastReadBAt END
        WHERE r.roomId = :roomId
          AND (:userNo = r.userANo OR :userNo = r.userBNo)
    """)
    int updateLastReadAt(
        @Param("roomId") Long roomId,
        @Param("userNo") Long userNo,
        @Param("readAt") LocalDateTime readAt
    );
}
