package dev.jpa.friends;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FriendsDTO {

    private Long friendId;
    private String requesterId;       // 요청 보낸 사람 (userid)
    private String receiverNickname;  // 닉네임으로 입력됨
    private String receiverId;        // 닉네임 → userid 변환 후 저장됨
    // 🔥 채팅용 (userNo)
    private Long requesterNo;
    private Long receiverNo;
    private Integer sw;

    public Friends toEntity() {
        Friends f = new Friends();
        f.setFriendId(friendId);
        f.setRequesterId(requesterId);
        f.setReceiverId(receiverId);
        f.setRequesterNo(requesterNo);
        f.setReceiverNo(receiverNo);
        f.setSw(sw);
        return f;
    }
}
