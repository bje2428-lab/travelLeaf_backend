package dev.jpa.user_chat_room;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserChatRoomDTO {

    private Long roomId;
    private Long userANo;
    private Long userBNo;
    private LocalDateTime createdAt;

    public UserChatRoom toEntity() {
        UserChatRoom entity = new UserChatRoom();
        entity.setRoomId(roomId);
        entity.setUserANo(userANo);
        entity.setUserBNo(userBNo);
        entity.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
        return entity;
    }

    public static UserChatRoomDTO fromEntity(UserChatRoom entity) {
        return new UserChatRoomDTO(
            entity.getRoomId(),
            entity.getUserANo(),
            entity.getUserBNo(),
            entity.getCreatedAt()
        );
    }
}
