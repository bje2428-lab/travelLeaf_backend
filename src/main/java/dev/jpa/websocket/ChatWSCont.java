package dev.jpa.websocket;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import dev.jpa.chat_message.ChatMessage;
import dev.jpa.chat_message.ChatMessageService;
import dev.jpa.chat_message.SenderType;
import dev.jpa.user_chat_message.UserChatMessage;
import dev.jpa.user_chat_message.UserChatMessageService;

// 🔥 FastAPI 연동 호텔 AI 서비스
import dev.jpa.websocket.HotelAiChatService;

@Controller
public class ChatWSCont {

    // 호텔 채팅 메시지
    @Autowired
    private ChatMessageService chatMessageService;

    // 유저 ↔ 유저 채팅 메시지
    @Autowired
    private UserChatMessageService userChatMessageService;

    // 🔥 호텔 AI 채팅 서비스 (FastAPI RAG 연동)
    @Autowired
    private HotelAiChatService hotelAiChatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatWSCont() {
        System.out.println("-> ChatWSCont created.");
    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageWS message) {

        try {
            Long roomId = message.getRoomId();
            String hotelExtId = message.getHotelExtId();

            // ==================================================
            // ✅ 유저 ↔ 유저 채팅 (기존 로직 그대로 유지)
            // ==================================================
            if (hotelExtId == null) {

                UserChatMessage saved = userChatMessageService.save(
                    roomId,
                    message.getSenderNo(),
                    message.getContent()
                );

                Map<String, Object> payload = Map.of(
                    "msgId", saved.getMsgId(),
                    "roomId", saved.getRoomId(),
                    "senderNo", saved.getSenderNo(),
                    "content", saved.getContent(),
                    "sentAt", saved.getSentAt()
                );

                messagingTemplate.convertAndSend(
                    "/topic/userroom/" + roomId,
                    payload
                );

                return; // 🔥 호텔 로직 차단
            }

            // ==================================================
            // ✅ 호텔 ↔ 유저 채팅 (RAG 기반 AI)
            // ==================================================

            SenderType senderType = SenderType.valueOf(message.getSenderType());

            // USER 메시지 저장
            ChatMessage userMessage = new ChatMessage(
                roomId,
                senderType,
                message.getSenderNo(),
                message.getContent()
            );
            userMessage.setSentAt(LocalDateTime.now());
            chatMessageService.save(userMessage);

            // USER 메시지 전송
            messagingTemplate.convertAndSend(
                "/topic/hotelroom/" + roomId,
                userMessage
            );

            // USER가 아니면 AI 응답 없음
            if (senderType != SenderType.USER) {
                return;
            }

            // 🔔 typing 시작
            messagingTemplate.convertAndSend(
                "/topic/hotelroom/" + roomId,
                Map.of("event", "TYPING_START", "roomId", roomId)
            );

            // 🔥 AI 응답은 비동기 처리
            new Thread(() -> {
                try {
                    // ===============================
                    // 🔥 FastAPI (RAG) 호출
                    // ===============================
                    String replyText = hotelAiChatService.ask(
                        hotelExtId,
                        message.getContent()
                    );

                    // typing 종료
                    messagingTemplate.convertAndSend(
                        "/topic/hotelroom/" + roomId,
                        Map.of("event", "TYPING_END", "roomId", roomId)
                    );

                    ChatMessage aiReply = new ChatMessage(
                        roomId,
                        SenderType.HOTEL,
                        null,
                        replyText
                    );
                    aiReply.setSentAt(LocalDateTime.now());
                    chatMessageService.save(aiReply);

                    messagingTemplate.convertAndSend(
                        "/topic/hotelroom/" + roomId,
                        aiReply
                    );

                } catch (Exception e) {
                    e.printStackTrace();

                    // 예외 발생 시 typing 종료라도 보장
                    messagingTemplate.convertAndSend(
                        "/topic/hotelroom/" + roomId,
                        Map.of("event", "TYPING_END", "roomId", roomId)
                    );
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
