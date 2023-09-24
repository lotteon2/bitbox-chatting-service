package com.bitbox.chatting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KafkaConsumeTestService {
    @KafkaListener(topics = "attendanceAlarm")
    public void updateQty(String kafkaMessage) {
        ObjectMapper mapper = new ObjectMapper();
        List<NotificationDto> notificationDtoList;
        try {
            notificationDtoList = mapper.readValue(kafkaMessage, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

        for (NotificationDto notificationDto : notificationDtoList) {
            log.info("memberId = {}", notificationDto.getMemberId());
        }

    }
}

@Getter
@NoArgsConstructor
class NotificationDto{
    private long memberId;
    private String messageType;
    private String message;
}