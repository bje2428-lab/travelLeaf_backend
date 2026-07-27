package dev.jpa.friends;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.user.User;
import dev.jpa.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendsService {

    private final FriendsRepository friendsRepo;
    private final UserRepository userRepo;

    /** 친구 요청 */
    public Friends request(FriendsDTO dto) {
        User receiver = userRepo.findByNickname(dto.getReceiverNickname())
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 유저가 존재하지 않습니다."));

        dto.setReceiverId(receiver.getUserid());

        if (dto.getRequesterId().equals(dto.getReceiverId())) {
            throw new IllegalStateException("자기 자신에게 친구 요청은 불가능합니다.");
        }

        friendsRepo.findFriendRelation(dto.getRequesterId(), dto.getReceiverId())
                .ifPresent(f -> {
                    throw new IllegalStateException("이미 친구 관계가 존재합니다.");
                });

        // 요청자
        User requester = userRepo.findByUserid(dto.getRequesterId())
                .orElseThrow(() -> new IllegalArgumentException("요청자 유저 없음"));

        dto.setRequesterNo(requester.getUserno());
        dto.setReceiverNo(receiver.getUserno());
        dto.setSw(0); // 요청중

        return friendsRepo.save(dto.toEntity());
    }

    /** 친구 수락 */
    public Friends accept(Long friendId) {
        Friends f = friendsRepo.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("친구 데이터가 존재하지 않습니다."));
        f.setSw(1);
        return f;
    }

    /** 친구 거절 */
    public Friends reject(Long friendId) {
        Friends f = friendsRepo.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("친구 데이터가 존재하지 않습니다."));
        f.setSw(2);
        return f;
    }

    /** 받은 요청 */
    @Transactional(readOnly = true)
    public List<FriendResponseDTO> receivedRequests(String userId) {
        return friendsRepo.findByReceiverIdAndSw(userId, 0)
                .stream()
                .map(f -> {
                    User requester = userRepo.findByUserid(f.getRequesterId())
                            .orElseThrow(() -> new RuntimeException("요청자 유저 없음"));

                    return new FriendResponseDTO(
                            f.getFriendId(),
                            requester.getUserid(),
                            requester.getUserno(),     // 🔥 핵심
                            requester.getNickname(),
                            f.getSw()
                    );
                }).toList();
    }

    /** 보낸 요청 */
    @Transactional(readOnly = true)
    public List<FriendResponseDTO> sentRequests(String userId) {
        return friendsRepo.findByRequesterIdAndSw(userId, 0)
                .stream()
                .map(f -> {
                    User receiver = userRepo.findByUserid(f.getReceiverId())
                            .orElseThrow(() -> new RuntimeException("수신자 유저 없음"));

                    return new FriendResponseDTO(
                            f.getFriendId(),
                            receiver.getUserid(),
                            receiver.getUserno(),      // 🔥 핵심
                            receiver.getNickname(),
                            f.getSw()
                    );
                }).toList();
    }

    /** 친구 목록 */
    @Transactional(readOnly = true)
    public List<FriendResponseDTO> myFriends(String userId) {
        return friendsRepo.findMyFriends(userId)
                .stream()
                .map(f -> {
                    String otherId =
                            f.getRequesterId().equals(userId)
                                    ? f.getReceiverId()
                                    : f.getRequesterId();

                    User other = userRepo.findByUserid(otherId)
                            .orElseThrow(() -> new RuntimeException("친구 유저 없음"));

                    return new FriendResponseDTO(
                            f.getFriendId(),
                            other.getUserid(),
                            other.getUserno(),         // 🔥 핵심
                            other.getNickname(),
                            f.getSw()
                    );
                }).toList();
    }

    /** 친구 삭제 */
    public void delete(Long friendId) {
        friendsRepo.deleteById(friendId);
    }

    // ==================================================
    // 🔥 채팅 전용
    // ==================================================

    /**
     * friendId + 내 userNo → 상대 userNo 반환
     */
    @Transactional(readOnly = true)
    public Long getTargetUserNoForChat(Long friendId, Long myUserNo) {

        Friends f = friendsRepo.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("친구 데이터가 존재하지 않습니다."));

        if (myUserNo.equals(f.getRequesterNo())) {
            return f.getReceiverNo();
        }

        if (myUserNo.equals(f.getReceiverNo())) {
            return f.getRequesterNo();
        }

        throw new IllegalStateException("해당 친구 관계에 포함되지 않은 사용자입니다.");
    }
}
