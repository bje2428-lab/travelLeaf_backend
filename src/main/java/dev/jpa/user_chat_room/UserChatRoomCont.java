package dev.jpa.user_chat_room;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userchatroom")
public class UserChatRoomCont {

    @Autowired
    private UserChatRoomService service;

    /** 회원 채팅방 생성 */
    @PostMapping("/create")
    public ResponseEntity<UserChatRoom> create(@RequestBody UserChatRoomDTO dto) {
        UserChatRoom room =
            service.createOrGetRoom(dto.getUserANo(), dto.getUserBNo());
        return ResponseEntity.ok(room);
    }

    /** 회원 채팅방 목록 */
    @GetMapping("/user/{userNo}")
    public ResponseEntity<List<UserChatRoomListDTO>> getByUser(
            @PathVariable("userNo") Long userNo) {

        return ResponseEntity.ok(service.findRoomListByUser(userNo));
    }

    /** 단건 조회 */
    @GetMapping("/read/{roomId}")
    public ResponseEntity<UserChatRoom> read(
            @PathVariable("roomId") Long roomId) {

        Optional<UserChatRoom> room = service.findById(roomId);
        return room.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 채팅방 입장 (읽음 처리) */
    @PostMapping("/{roomId}/enter")
    public ResponseEntity<Void> enterRoom(
            @PathVariable("roomId") Long roomId,
            @RequestParam("userNo") Long userNo) {

        service.enterRoom(roomId, userNo);
        return ResponseEntity.ok().build();
    }

    /** 채팅방 삭제
     * DELETE http://localhost:9100/userchatroom/{roomId}
     */
    @DeleteMapping("delete/{roomId}")
    public ResponseEntity<Integer> delete(
            @PathVariable("roomId") Long roomId) {

        return ResponseEntity.ok(service.deleteRoom(roomId) ? 1 : 0);
    }
}
