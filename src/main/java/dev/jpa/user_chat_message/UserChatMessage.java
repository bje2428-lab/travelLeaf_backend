package dev.jpa.user_chat_message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "USER_CHAT_MESSAGE")
public class UserChatMessage {

    /** 메시지 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_chat_message_seq")
    @SequenceGenerator(
        name = "user_chat_message_seq",
        sequenceName = "USER_CHAT_MESSAGE_SEQ",
        allocationSize = 1
    )
    private Long msgId;

    /** 회원 채팅방 ID */
    @Column(name = "ROOM_ID", nullable = false)
    private Long roomId;

    /** 메시지 보낸 회원 번호 */
    @Column(name = "SENDER_NO", nullable = false)
    private Long senderNo;

    /** 메시지 내용 */
    @Column(name = "CONTENT", nullable = false)
    private String content;

    /** 메시지 전송 시간 */
    @Column(name = "SENT_AT")
    private LocalDateTime sentAt = LocalDateTime.now();

    /** 기본 생성자 */
    public UserChatMessage() { }

    /** 편의 생성자 */
    public UserChatMessage(Long roomId, Long senderNo, String content) {
        this.roomId = roomId;
        this.senderNo = senderNo;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }
}
