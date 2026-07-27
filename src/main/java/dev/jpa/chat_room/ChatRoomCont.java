package dev.jpa.chat_room;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hotelchatroom")
public class ChatRoomCont {

    @Autowired
    private ChatRoomService chatRoomService;

    public ChatRoomCont() {
        System.out.println("-> ChatRoomController created.");
    }

    /** 채팅방 생성 (존재 시 조회)
     * http://localhost:9100/chatroom/create
     * @param dto
     * @return
     */
    @PostMapping("/create")
    public ResponseEntity<ChatRoom> create(@RequestBody ChatRoomDTO dto) {
        try {
            ChatRoom room = chatRoomService.createOrGetRoom(
                dto.getUserNo(),
                dto.getHotelExtId(),
                dto.getHotelName()
            );
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /** 특정 유저 채팅방 조회
     * http://localhost:9100/chatroom/user/17
     * @param userNo
     * @return
     */
    @GetMapping("/user/{userNo}")
    public ResponseEntity<List<ChatRoomListDTO>> getByUser(
            @PathVariable("userNo") Long userNo) {

        List<ChatRoomListDTO> list =
                chatRoomService.findRoomListByUser(userNo);

        return ResponseEntity.ok(list);
    }

    /** 특정 호텔 채팅방 조회
     * http://localhost:9100/chatroom/hotel/1
     * @param hotelExtId
     * @return
     */
    @GetMapping("/hotel/{hotelExtId}")
    public ResponseEntity<List<ChatRoom>> getByHotel(
            @PathVariable("hotelExtId") String hotelExtId) {

        List<ChatRoom> list =
                chatRoomService.findRoomsByHotel(hotelExtId);

        return ResponseEntity.ok(list);
    }

    /** 채팅방 단건 조회
     * http://localhost:9100/chatroom/read/1
     * @param roomId
     * @return
     */
    @GetMapping("/read/{roomId}")
    public ResponseEntity<ChatRoom> read(
            @PathVariable("roomId") Long roomId) {

        Optional<ChatRoom> room =
                chatRoomService.findById(roomId);

        return room
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 채팅방 삭제
     * http://localhost:9100/chatroom/2
     * @param roomId
     * @return
     */
    @DeleteMapping("delete/{roomId}")
    public ResponseEntity<Integer> delete(
            @PathVariable("roomId") Long roomId) {

        boolean result = chatRoomService.deleteRoom(roomId);
        return ResponseEntity.ok(result ? 1 : 0);
    }
}
