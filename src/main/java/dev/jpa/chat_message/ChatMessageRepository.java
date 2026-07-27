package dev.jpa.chat_message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** 특정 채팅방 메시지 조회 (시간순) */
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
    
    /** ⭐ 특정 채팅방의 가장 최근 메시지 1건 */
    ChatMessage findTopByRoomIdOrderBySentAtDesc(Long roomId);
    
    /** 특정 유저가 보낸 메시지 조회 */
    List<ChatMessage> findBySenderNoOrderBySentAtAsc(Long senderNo);

    /**
     * 메시지 내용 수정
     */
    @Modifying
    @Transactional
    @Query(
        value = "UPDATE CHAT_MESSAGE " +
                "SET CONTENT = :content " +
                "WHERE MSG_ID = :msgId",
        nativeQuery = true
    )
    int updateContent(@Param("content") String content,
                      @Param("msgId") Long msgId);

    /**
     * ✅ 특정 채팅방(roomId)에 속한 메시지 전체 삭제
     * - 채팅방 삭제 전에 반드시 호출
     */
    @Modifying
    @Transactional
    @Query(
        value = "DELETE FROM CHAT_MESSAGE WHERE ROOM_ID = :roomId",
        nativeQuery = true
    )
    void deleteByRoomId(@Param("roomId") Long roomId);
}
