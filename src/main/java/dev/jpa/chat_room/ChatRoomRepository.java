package dev.jpa.chat_room;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /** 특정 유저의 채팅방 조회 */
    List<ChatRoom> findByUserNoOrderByRoomIdAsc(Long userNo);

    /** 특정 호텔 관련 채팅방 조회 */
    List<ChatRoom> findByHotelExtIdOrderByRoomIdAsc(String hotelExtId);

    /** 유저와 호텔 기준 채팅방 조회 (1:1 매칭) */
    Optional<ChatRoom> findByUserNoAndHotelExtId(Long userNo, String hotelExtId);

    /**
     * 채팅방 호텔 ID 변경 (예시)  
     * 필요시 nativeQuery 활용 가능
     */
    @Modifying
    @Query(
        value = "UPDATE CHAT_ROOM SET HOTEL_EXT_ID = :hotelExtId WHERE ROOM_ID = :roomId",
        nativeQuery = true
    )
    int updateHotelExtId(@Param("hotelExtId") String hotelExtId,
                         @Param("roomId") Long roomId);
}
