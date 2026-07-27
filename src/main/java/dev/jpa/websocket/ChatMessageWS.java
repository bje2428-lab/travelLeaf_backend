package dev.jpa.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessageWS {
    private Long roomId;
    private String hotelExtId;
    private String senderType; // USER / HOTEL
    private Long senderNo;
    private String content;
}
