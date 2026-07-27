package dev.jpa.chat_room;

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
public class ChatRoomDTO {

    private Long roomId;

    private Long userNo;

    private String hotelExtId;
    
    private String hotelName;

    private LocalDateTime createdAt;

    /** DTO → Entity 변환 */
    public ChatRoom toEntity() {
        ChatRoom entity = new ChatRoom();
        entity.setRoomId(roomId);
        entity.setUserNo(userNo);
        entity.setHotelExtId(hotelExtId);
        entity.setHotelName(hotelName);
        entity.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
        return entity;
    }

    /** Entity → DTO 변환 */
    public static ChatRoomDTO fromEntity(ChatRoom entity) {
        return new ChatRoomDTO(
                entity.getRoomId(),
                entity.getUserNo(),
                entity.getHotelExtId(),
                entity.getHotelName(),
                entity.getCreatedAt()
        );
    }
}
