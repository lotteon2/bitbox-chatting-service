package com.bitbox.chatting.kafka;

import com.bitbox.chatting.service.response.ChatRoomCreationEventResponse;
import com.bitbox.chatting.service.response.RealTimeChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaListenerClass {
    private final SimpMessageSendingOperations messagingTemplate;
    private final long ChattingRoomCreationEventNumber = 0L;

    @KafkaListener(topics = "${chatTopicName}")
    public void consumeChatMessage(RealTimeChatResponse response) {
        messagingTemplate.convertAndSend("/room/" + response.getChatRoomId(), response);
    }

    @KafkaListener(topics = "${chatCreationTopicName}")
    public void consumeChatCreationMessage(ChatRoomCreationEventResponse chatRoomCreationEventResponse) {
        messagingTemplate.convertAndSend("/room/" + ChattingRoomCreationEventNumber, chatRoomCreationEventResponse);
    }
}
