package dev.jpa.chat_message;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessageService() {
        System.out.println("-> ChatMessageService created");
    }

    /** 메시지 등록 */
    public ChatMessage save(ChatMessage message) {
        ChatMessage saved = chatMessageRepository.save(message);
        System.out.println("-> msgId: " + saved.getMsgId());
        return saved;
    }

    /** 특정 채팅방 메시지 조회 (시간순) */
    public List<ChatMessage> findByRoomId(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    /** 특정 유저가 보낸 메시지 조회 */
    public List<ChatMessage> findBySenderNo(Long senderNo) {
        return chatMessageRepository.findBySenderNoOrderBySentAtAsc(senderNo);
    }

    /** 단건 조회 */
    public Optional<ChatMessage> findById(Long msgId) {
        return chatMessageRepository.findById(msgId);
    }

    /** 메시지 내용 수정 */
    @Transactional
    public int updateContent(Long msgId, String content) {
        return chatMessageRepository.updateContent(content, msgId);
    }

    /** 메시지 삭제 */
    @Transactional
    public boolean delete(Long msgId) {
        try {
            chatMessageRepository.deleteById(msgId);
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting ChatMessage: " + e.toString());
            return false;
        }
    }
}
