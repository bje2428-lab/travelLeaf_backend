package dev.jpa.chat_room;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import dev.jpa.chat_message.ChatMessage;
import dev.jpa.chat_message.ChatMessageRepository;
import dev.jpa.chat_message.SenderType;

import jakarta.transaction.Transactional;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /** 🔥 WebSocket 전송용 */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatRoomService() {
        System.out.println("-> ChatRoomService created");
    }

    /** 채팅방 생성 (존재 시 조회) */
    @Transactional
    public ChatRoom createOrGetRoom(Long userNo, String hotelExtId, String hotelName) {

        Optional<ChatRoom> existing =
            chatRoomRepository.findByUserNoAndHotelExtId(userNo, hotelExtId);

        // 🔹 이미 채팅방이 있으면 그냥 반환 (자동 인사 ❌)
        if (existing.isPresent()) {
            return existing.get();
        }

        // 🔹 채팅방 최초 생성
        ChatRoom room = new ChatRoom(userNo, hotelExtId, hotelName);
        ChatRoom savedRoom = chatRoomRepository.save(room);

        // 🔹 자동 인사 메시지 생성
        ChatMessage welcome = new ChatMessage(
            savedRoom.getRoomId(),
            SenderType.HOTEL,     // 🔥 호텔이 보낸 메시지
            null,                // 호텔은 senderNo 없음
            "안녕하세요 😊\n" +
            hotelName + "입니다.\n" +
            "무엇을 도와드릴까요?"
        );

        chatMessageRepository.save(welcome);

        // 🔹 WebSocket으로 바로 전송
        messagingTemplate.convertAndSend(
            "/topic/room/" + savedRoom.getRoomId(),
            welcome
        );

        System.out.println("-> Created ChatRoom + Welcome Message: " + savedRoom.getRoomId());

        return savedRoom;
    }

    /** 채팅방 목록 + 최근 메시지 */
    public List<ChatRoomListDTO> findRoomListByUser(Long userNo) {

        List<ChatRoom> rooms =
            chatRoomRepository.findByUserNoOrderByRoomIdAsc(userNo);

        return rooms.stream().map(room -> {
            ChatRoomListDTO dto = new ChatRoomListDTO();
            dto.setRoomId(room.getRoomId());
            dto.setHotelExtId(room.getHotelExtId());
            dto.setHotelName(room.getHotelName());

            ChatMessage last =
                chatMessageRepository.findTopByRoomIdOrderBySentAtDesc(
                    room.getRoomId()
                );

            if (last != null) {
                dto.setLastMessage(last.getContent());
                dto.setLastSentAt(last.getSentAt());
            }

            return dto;
        }).toList();
    }

    public Optional<ChatRoom> findById(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }

    public List<ChatRoom> findRoomsByHotel(String hotelExtId) {
        return chatRoomRepository.findByHotelExtIdOrderByRoomIdAsc(hotelExtId);
    }
    
      /**
       * ✅ 채팅방 삭제
       * - 해당 채팅방의 모든 메시지 먼저 삭제
       * - 이후 채팅방 삭제
       */
      @Transactional
      public boolean deleteRoom(Long roomId) {
          try {
              // 🔥 1) 채팅 메시지 전체 삭제
              chatMessageRepository.deleteByRoomId(roomId);

              // 🔥 2) 채팅방 삭제
              chatRoomRepository.deleteById(roomId);

              return true;
          } catch (Exception e) {
              e.printStackTrace();
              return false;
          }
      }

}
