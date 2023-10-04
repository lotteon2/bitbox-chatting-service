package com.bitbox.chatting.service;

import com.bitbox.chatting.repository.ChatRepository;
import io.github.bitbox.bitbox.dto.SubscriptionExpireDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchProcessingService {
    private final ChatRepository chatRepository;
    @KafkaListener(topics = "${expirationTopicName}")
    @Transactional
    public void kafkaTopicTest(SubscriptionExpireDto subscriptionExpireDto) {
        chatRepository.updateChatIsPaidByHostAndCreatedAt(subscriptionExpireDto.getMemberId(), subscriptionExpireDto.getEndDate());
    }
}