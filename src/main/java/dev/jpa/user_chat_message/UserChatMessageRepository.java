package dev.jpa.user_chat_message;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface UserChatMessageRepository
        extends JpaRepository<UserChatMessage, Long> {

    /* ===============================
       기본 조회
    ================================ */

    /** 특정 채팅방 메시지 조회 (시간순) */
    List<UserChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);

    /** 특정 채팅방의 가장 최근 메시지 1건 */
    UserChatMessage findTopByRoomIdOrderBySentAtDesc(Long roomId);

    /** 특정 회원이 보낸 메시지 조회 */
    List<UserChatMessage> findBySenderNoOrderBySentAtAsc(Long senderNo);

    /* ===============================
       안 읽은 메시지 관련 (🔥 핵심)
    ================================ */

    /**
     * 마지막 읽은 시각 이후 안 읽은 메시지 개수
     * (상대방이 보낸 것만)
     */
    @Query("""
        SELECT COUNT(m)
        FROM UserChatMessage m
        WHERE m.roomId = :roomId
          AND m.senderNo <> :myNo
          AND m.sentAt > :lastReadAt
    """)
    int countUnreadMessages(
        @Param("roomId") Long roomId,
        @Param("myNo") Long myNo,
        @Param("lastReadAt") LocalDateTime lastReadAt
    );

    /**
     * 🔥 채팅방을 한 번도 열지 않은 경우
     * → 해당 방의 "내가 보낸 것 제외" 전체 메시지 수
     */
    int countByRoomIdAndSenderNoNot(Long roomId, Long senderNo);

    /* ===============================
       메시지 수정 (예시)
    ================================ */

    /**
     * 메시지 내용 수정
     * (필요 시 사용)
     */
    @Modifying
    @Query(
        value = "UPDATE USER_CHAT_MESSAGE " +
                "SET CONTENT = :content " +
                "WHERE MSG_ID = :msgId",
        nativeQuery = true
    )
    int updateContent(
        @Param("content") String content,
        @Param("msgId") Long msgId
    );
    
    /* ===============================
        채팅방 삭제용 (🔥 추가)
     =============================== */
    
     /**
      * 특정 채팅방의 메시지 전체 삭제
      * - 채팅방 삭제 전에 호출
      */
    @Modifying
    @Transactional
    @Query(
        value = "DELETE FROM USER_CHAT_MESSAGE WHERE ROOM_ID = :roomId",
        nativeQuery = true
    )
    void deleteByRoomId(@Param("roomId") Long roomId);


}
