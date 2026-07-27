package dev.jpa.chat_message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "CHAT_MESSAGE")
public class ChatMessage {

    /** 메시지 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_message_seq")
    @SequenceGenerator(name = "chat_message_seq", sequenceName = "CHAT_MESSAGE_SEQ", allocationSize = 1)
    private Long msgId;

    /** 채팅방 ID */
    @Column(name = "ROOM_ID", nullable = false)
    private Long roomId;

    /** 메시지 보낸 주체(USER/HOTEL) */
    @Enumerated(EnumType.STRING)
    @Column(name = "SENDER_TYPE", nullable = false)
    private SenderType senderType;

    /** 유저 번호 (호텔이면 null) */
    @Column(name = "SENDER_NO")
    private Long senderNo;

    /** 메시지 내용 */
    @Column(name = "CONTENT", nullable = false)
    private String content;

    /** 메시지 전송 시간 */
    @Column(name = "SENT_AT")
    private LocalDateTime sentAt = LocalDateTime.now();

    /** 기본 생성자 */
    public ChatMessage() { }

    /** 편의 생성자 */
    public ChatMessage(Long roomId, SenderType senderType, Long senderNo, String content) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNo = senderNo;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }
}
