package dev.jpa.user_chat_room;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserChatRoomListDTO {

    private Long roomId;

    /** 상대 회원 번호 (프론트에서 프로필 조회용) */
    private Long otherUserNo;
    
    /** 상대 회원 닉네임 */
    private String otherNickname;

    /** 최근 메시지 */
    private String lastMessage;

    /** 최근 메시지 시간 */
    private LocalDateTime lastSentAt;
    
    /** 안읽은 메시지 개수 */
    private int unreadCount;
}
