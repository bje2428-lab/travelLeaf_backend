package dev.jpa.chat_room;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatRoomListDTO {

    private Long roomId;
    private String hotelExtId;
    private String hotelName;

    /** 최근 메시지 */
    private String lastMessage;

    /** 최근 메시지 시간 */
    private LocalDateTime lastSentAt;
}
