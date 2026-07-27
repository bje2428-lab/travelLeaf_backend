package dev.jpa.user_chat_message;

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
public class UserChatMessageDTO {

    private Long msgId;

    private Long roomId;

    private Long senderNo;

    private String content;

    private LocalDateTime sentAt;

    /** DTO → Entity 변환 */
    public UserChatMessage toEntity() {
        UserChatMessage entity = new UserChatMessage();
        entity.setMsgId(msgId);
        entity.setRoomId(roomId);
        entity.setSenderNo(senderNo);
        entity.setContent(content);
        entity.setSentAt(sentAt != null ? sentAt : LocalDateTime.now());
        return entity;
    }

    /** Entity → DTO 변환 */
    public static UserChatMessageDTO fromEntity(UserChatMessage entity) {
        return new UserChatMessageDTO(
                entity.getMsgId(),
                entity.getRoomId(),
                entity.getSenderNo(),
                entity.getContent(),
                entity.getSentAt()
        );
    }
}
