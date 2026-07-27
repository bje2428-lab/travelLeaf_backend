package dev.jpa.chat_message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessageDTO {

    private Long msgId;

    private Long roomId;

    private String senderType; // USER / HOTEL

    private Long senderNo;

    private String content;

    private LocalDateTime sentAt;

    /** DTO → Entity 변환 */
    public ChatMessage toEntity() {
        ChatMessage entity = new ChatMessage();
        entity.setMsgId(msgId);
        entity.setRoomId(roomId);
        entity.setSenderType(senderType != null ? SenderType.valueOf(senderType) : null);
        entity.setSenderNo(senderNo);
        entity.setContent(content);
        entity.setSentAt(sentAt != null ? sentAt : LocalDateTime.now());
        return entity;
    }

    /** Entity → DTO 변환 */
    public static ChatMessageDTO fromEntity(ChatMessage entity) {
        return new ChatMessageDTO(
                entity.getMsgId(),
                entity.getRoomId(),
                entity.getSenderType().name(),
                entity.getSenderNo(),
                entity.getContent(),
                entity.getSentAt()
        );
    }
}
