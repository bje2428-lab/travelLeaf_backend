package dev.jpa.friends;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendsCont {

    private final FriendsService friendsService;

    /** 친구 요청 */
    @PostMapping("/request")
    public ResponseEntity<Friends> request(@RequestBody FriendsDTO dto) {
        return ResponseEntity.ok(friendsService.request(dto));
    }

    /** 친구 수락 */
    @PutMapping("/accept/{id}")
    public ResponseEntity<Friends> accept(@PathVariable("id") Long id) {
        return ResponseEntity.ok(friendsService.accept(id));
    }

    /** 친구 거절 */
    @PutMapping("/reject/{id}")
    public ResponseEntity<Friends> reject(@PathVariable("id") Long id) {
        return ResponseEntity.ok(friendsService.reject(id));
    }

    /** 받은 요청 */
    @GetMapping("/received/{userId}")
    public ResponseEntity<List<FriendResponseDTO>> received(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(friendsService.receivedRequests(userId));
    }

    /** 보낸 요청 */
    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<FriendResponseDTO>> sent(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(friendsService.sentRequests(userId));
    }

    /** 친구 목록 */
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<FriendResponseDTO>> list(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(friendsService.myFriends(userId));
    }

    /** 친구 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        friendsService.delete(id);
        return ResponseEntity.ok().build();
    }
    
 // ==================================================
    // 🔥 채팅 전용 API (추가된 유일한 부분)
    // ==================================================

    /**
     * 친구 페이지 → 채팅 버튼 클릭
     * friendId + myUserNo → 상대 userNo 반환
     *
     * userid ❌
     * DTO ❌
     */
    @GetMapping("/chat/target-no")
    public ResponseEntity<Long> getChatTargetUserNo(
            @RequestParam("friendId") Long friendId,
            @RequestParam("myUserNo") Long myUserNo
    ) {
        Long targetUserNo =
                friendsService.getTargetUserNoForChat(friendId, myUserNo);
        return ResponseEntity.ok(targetUserNo);
    }
}
