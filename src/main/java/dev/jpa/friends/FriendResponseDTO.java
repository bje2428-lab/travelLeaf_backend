package dev.jpa.friends;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FriendResponseDTO {
    private Long friendId;
    private String userId;       // 상대방 userid
    private Long friendUserNo;
    private String nickname;     // 상대방 닉네임
    private Integer sw;          // 상태
}