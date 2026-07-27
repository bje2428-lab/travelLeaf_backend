package dev.jpa.user_chat_message;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserChatMessageService {

    @Autowired
    private UserChatMessageRepository userChatMessageRepository;

    public UserChatMessageService() {
        System.out.println("-> UserChatMessageService created");
    }

    /** 메시지 등록 */
    public UserChatMessage save(UserChatMessage message) {
        UserChatMessage saved = userChatMessageRepository.save(message);
        System.out.println("-> user msgId: " + saved.getMsgId());
        return saved;
    }

    /** 편의 저장 메서드 */
    public UserChatMessage save(Long roomId, Long senderNo, String content) {
        return save(new UserChatMessage(roomId, senderNo, content));
    }

    /** 특정 채팅방 메시지 조회 (시간순) */
    public List<UserChatMessage> findByRoomId(Long roomId) {
        return userChatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    /** 특정 회원이 보낸 메시지 조회 */
    public List<UserChatMessage> findBySenderNo(Long senderNo) {
        return userChatMessageRepository.findBySenderNoOrderBySentAtAsc(senderNo);
    }

    /** 단건 조회 */
    public Optional<UserChatMessage> findById(Long msgId) {
        return userChatMessageRepository.findById(msgId);
    }

    /** 메시지 내용 수정 */
    @Transactional
    public int updateContent(Long msgId, String content) {
        return userChatMessageRepository.updateContent(content, msgId);
    }

    /** 메시지 삭제 */
    @Transactional
    public boolean delete(Long msgId) {
        try {
            userChatMessageRepository.deleteById(msgId);
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting UserChatMessage: " + e.toString());
            return false;
        }
    }
}
