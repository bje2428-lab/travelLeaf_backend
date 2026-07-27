package dev.jpa.user_chat_room;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "USER_CHAT_ROOM")
public class UserChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_chat_room_seq")
    @SequenceGenerator(
        name = "user_chat_room_seq",
        sequenceName = "USER_CHAT_ROOM_SEQ",
        allocationSize = 1
    )
    private Long roomId;

    /** 채팅 참여 회원 A */
    @Column(name = "USER_A_NO", nullable = false)
    private Long userANo;

    /** 채팅 참여 회원 B */
    @Column(name = "USER_B_NO", nullable = false)
    private Long userBNo;
    
    /** A가 마지막으로 읽은 시간 */
    @Column(name = "LAST_READ_A_AT")
    private LocalDateTime lastReadAAt;

    /** B가 마지막으로 읽은 시간 */
    @Column(name = "LAST_READ_B_AT")
    private LocalDateTime lastReadBAt;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();
}
