package dev.jpa.user_chat_room;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.jpa.user.UserRepository;
import dev.jpa.user_chat_message.UserChatMessage;
import dev.jpa.user_chat_message.UserChatMessageRepository;
import jakarta.transaction.Transactional;

@Service
public class UserChatRoomService {

    @Autowired
    private UserChatRoomRepository roomRepository;

    @Autowired
    private UserChatMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /** 유저 ↔ 유저 채팅방 생성 or 기존 방 */
    @Transactional
    public UserChatRoom createOrGetRoom(Long userANo, Long userBNo) {
        return roomRepository.findByUsers(userANo, userBNo)
            .orElseGet(() -> {
                UserChatRoom room = new UserChatRoom();
                room.setUserANo(userANo);
                room.setUserBNo(userBNo);
                return roomRepository.save(room);
            });
    }

    /** 채팅방 리스트 */
    public List<UserChatRoomListDTO> findRoomListByUser(Long userNo) {

        List<UserChatRoom> rooms = roomRepository.findByUserNo(userNo);

        return rooms.stream().map(room -> {
            UserChatRoomListDTO dto = new UserChatRoomListDTO();
            dto.setRoomId(room.getRoomId());

            // 1️⃣ 상대방
            Long otherUserNo =
                room.getUserANo().equals(userNo)
                    ? room.getUserBNo()
                    : room.getUserANo();

            dto.setOtherUserNo(otherUserNo);

            userRepository.findById(otherUserNo)
                .ifPresent(u -> dto.setOtherNickname(u.getNickname()));

            // 2️⃣ 최근 메시지
            UserChatMessage last =
                messageRepository.findTopByRoomIdOrderBySentAtDesc(room.getRoomId());

            if (last != null) {
                dto.setLastMessage(last.getContent());
                dto.setLastSentAt(last.getSentAt());
            }

            // 3️⃣ 안 읽은 메시지 개수 (🔥 핵심)
            LocalDateTime lastReadAt =
                room.getUserANo().equals(userNo)
                    ? room.getLastReadAAt()
                    : room.getLastReadBAt();

            int unreadCount;

            if (lastReadAt == null) {
                // 한 번도 열지 않은 방
                unreadCount = messageRepository.countByRoomIdAndSenderNoNot(
                    room.getRoomId(),
                    userNo
                );
            } else {
                unreadCount = messageRepository.countUnreadMessages(
                    room.getRoomId(),
                    userNo,
                    lastReadAt
                );
            }

            dto.setUnreadCount(unreadCount);
            return dto;

        }).toList();
    }

    /** 채팅방 입장 (읽음 처리) */
    @Transactional
    public void enterRoom(Long roomId, Long userNo) {
        roomRepository.updateLastReadAt(
            roomId,
            userNo,
            LocalDateTime.now()
        );
    }

    /** 단건 조회 */
    public Optional<UserChatRoom> findById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    /** 채팅방 삭제 */
    @Transactional
    public boolean deleteRoom(Long roomId) {
        try {
            if (!roomRepository.existsById(roomId)) return false;

            // 🔥 1) 해당 채팅방의 모든 메시지 삭제
            messageRepository.deleteByRoomId(roomId);

            // 🔥 2) 채팅방 삭제
            roomRepository.deleteById(roomId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
