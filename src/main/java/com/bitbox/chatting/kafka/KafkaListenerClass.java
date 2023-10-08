package com.bitbox.chatting.kafka;

import com.bitbox.chatting.repository.ChatRepository;
import com.bitbox.chatting.service.response.ChatRoomCreationEventResponse;
import com.bitbox.chatting.service.response.RealTimeChatResponse;
import io.github.bitbox.bitbox.dto.SubscriptionExpireDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class KafkaListenerClass {
    private final ChatRepository chatRepository;
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

    @KafkaListener(topics = "${expirationTopicName}")
    @Transactional
    public void kafkaTopicTest(SubscriptionExpireDto subscriptionExpireDto) {
        chatRepository.updateChatIsPaidByHostAndCreatedAt(subscriptionExpireDto.getMemberId(), subscriptionExpireDto.getEndDate());
    }
}
