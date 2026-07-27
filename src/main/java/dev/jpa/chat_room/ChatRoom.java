package dev.jpa.chat_room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "CHAT_ROOM")
public class ChatRoom {

  /** 채팅방 고유 ID */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_room_seq")
  @SequenceGenerator(name = "chat_room_seq", sequenceName = "CHAT_ROOM_SEQ", allocationSize = 1)
  private Long roomId;

  /** 유저 번호 (USER_TB PK) */
  @Column(name = "USER_NO", nullable = false)
  private Long userNo;

  /** 호텔 외부 ID (카카오/네이버 place_id) */
  @Column(name = "HOTEL_EXT_ID", nullable = false)
  private String hotelExtId;

  /** 호텔 이름 */
  @Column(name = "HOTEL_NAME", nullable = false)
  private String hotelName;

  /** 채팅방 생성일 */
  @Column(name = "CREATED_AT")
  private LocalDateTime createdAt = LocalDateTime.now();

  /** 기본 생성자 */
  public ChatRoom() {
  }

  public ChatRoom(Long userNo, String hotelExtId, String hotelName) {
    this.userNo = userNo;
    this.hotelExtId = hotelExtId;
    this.hotelName = hotelName;
    this.createdAt = LocalDateTime.now();
  }
}
