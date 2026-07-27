package dev.jpa.chat_message;

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
@RequestMapping("/chatmessage")
public class ChatMessageCont {

    @Autowired
    private ChatMessageService chatMessageService;

    public ChatMessageCont() {
        System.out.println("-> ChatMessageCont created.");
    }

    /** 메시지 전송
     * http://localhost:9100/chatmessage/send
     */
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody ChatMessageDTO dto) {
        try {
            ChatMessage saved = chatMessageService.save(dto.toEntity());
            return ResponseEntity.ok(ChatMessageDTO.fromEntity(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /** 특정 채팅방 메시지 조회
     * http://localhost:9100/chatmessage/room/1
     * @param roomId
     * @return
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatMessageDTO>> getByRoom(@PathVariable("roomId") Long roomId) {
        List<ChatMessage> list = chatMessageService.findByRoomId(roomId);
        List<ChatMessageDTO> dtoList = list.stream()
                                           .map(ChatMessageDTO::fromEntity)
                                           .toList();
        return ResponseEntity.ok(dtoList);
    }

    /** 특정 유저가 보낸 메시지 조회
     * http://localhost:9100/chatmessage/user/17
     * @param userNo
     * @return
     */
    @GetMapping("/user/{userNo}")
    public ResponseEntity<List<ChatMessageDTO>> getByUser(@PathVariable("userNo") Long userNo) {
        List<ChatMessage> list = chatMessageService.findBySenderNo(userNo);
        List<ChatMessageDTO> dtoList = list.stream()
                                           .map(ChatMessageDTO::fromEntity)
                                           .toList();
        return ResponseEntity.ok(dtoList);
    }

    /** 단건 메시지 조회
     * http://localhost:9100/chatmessage/read/1
     * @param msgId
     * @return
     */
    @GetMapping("/read/{msgId}")
    public ResponseEntity<ChatMessageDTO> read(@PathVariable("msgId") Long msgId) {
        Optional<ChatMessage> msg = chatMessageService.findById(msgId);
        return msg.map(m -> ResponseEntity.ok(ChatMessageDTO.fromEntity(m)))
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 메시지 내용 수정
     * http://localhost:9100/chatmessage/update
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody ChatMessageDTO dto) {
        int cnt = chatMessageService.updateContent(dto.getMsgId(), dto.getContent());
        return ResponseEntity.ok(cnt);
    }

    /** 메시지 삭제
     * http://localhost:9100/chatmessage/delete/3
     * @param msgId
     * @return
     */
    @DeleteMapping("/delete/{msgId}")
    public ResponseEntity<Integer> delete(@PathVariable("msgId") Long msgId) {
        boolean result = chatMessageService.delete(msgId);
        return ResponseEntity.ok(result ? 1 : 0);
    }
}
