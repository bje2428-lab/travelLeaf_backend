package dev.jpa.user_chat_message;

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
@RequestMapping("/userchatmessage")
public class UserChatMessageCont {

    @Autowired
    private UserChatMessageService userChatMessageService;

    public UserChatMessageCont() {
        System.out.println("-> UserChatMessageCont created.");
    }

    /** 메시지 전송
     * http://localhost:9100/userchatmessage/send
     */
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody UserChatMessageDTO dto) {
        try {
            UserChatMessage saved =
                userChatMessageService.save(dto.toEntity());
            return ResponseEntity.ok(UserChatMessageDTO.fromEntity(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /** 특정 채팅방 메시지 조회
     * http://localhost:9100/userchatmessage/room/1
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<UserChatMessageDTO>> getByRoom(
            @PathVariable("roomId") Long roomId) {

        List<UserChatMessage> list =
            userChatMessageService.findByRoomId(roomId);

        List<UserChatMessageDTO> dtoList =
            list.stream()
                .map(UserChatMessageDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /** 특정 유저가 보낸 메시지 조회
     * http://localhost:9100/userchatmessage/user/17
     */
    @GetMapping("/user/{userNo}")
    public ResponseEntity<List<UserChatMessageDTO>> getByUser(
            @PathVariable("userNo") Long userNo) {

        List<UserChatMessage> list =
            userChatMessageService.findBySenderNo(userNo);

        List<UserChatMessageDTO> dtoList =
            list.stream()
                .map(UserChatMessageDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /** 단건 메시지 조회
     * http://localhost:9100/userchatmessage/read/1
     */
    @GetMapping("/read/{msgId}")
    public ResponseEntity<UserChatMessageDTO> read(
            @PathVariable("msgId") Long msgId) {

        Optional<UserChatMessage> msg =
            userChatMessageService.findById(msgId);

        return msg.map(m ->
                ResponseEntity.ok(UserChatMessageDTO.fromEntity(m)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 메시지 내용 수정
     * http://localhost:9100/userchatmessage/update
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(
            @RequestBody UserChatMessageDTO dto) {

        int cnt =
            userChatMessageService.updateContent(
                dto.getMsgId(),
                dto.getContent()
            );

        return ResponseEntity.ok(cnt);
    }

    /** 메시지 삭제
     * http://localhost:9100/userchatmessage/delete/3
     */
    @DeleteMapping("/delete/{msgId}")
    public ResponseEntity<Integer> delete(
            @PathVariable("msgId") Long msgId) {

        boolean result =
            userChatMessageService.delete(msgId);

        return ResponseEntity.ok(result ? 1 : 0);
    }
}
