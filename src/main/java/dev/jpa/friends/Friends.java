package dev.jpa.friends;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FRIENDS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "friends_seq")
    @SequenceGenerator(
        name = "friends_seq",
        sequenceName = "FRIENDS_SEQ",
        allocationSize = 1
    )
    @Column(name = "FRIEND_ID")
    private Long friendId;

    @Column(name = "REQUESTER_ID", nullable = false, length = 50)
    private String requesterId;

    @Column(name = "RECEIVER_ID", nullable = false, length = 50)
    private String receiverId;
    
 // 🔥 채팅용: 요청자 userNo
    @Column(name = "REQUESTER_NO", nullable = false)
    private Long requesterNo;

    // 🔥 채팅용: 수신자 userNo
    @Column(name = "RECEIVER_NO", nullable = false)
    private Long receiverNo;

    /** 
     * 0: 요청중
     * 1: 수락
     * 2: 거절
     * 3: 차단
     */
    @Column(name = "SW", nullable = false)
    private Integer sw;

    


  
}
