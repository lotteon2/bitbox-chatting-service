package com.bitbox.chatting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bitbox.bitbox.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class KafkaConsumeTestService {
    @KafkaListener(topics = "attendanceAlarm")
    public void kafkaTopicTest(String kafkaMessage) {
        ObjectMapper mapper = new ObjectMapper();
        NotificationDto notificationDto;
        try {
            notificationDto = mapper.readValue(kafkaMessage, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

        log.info("memberId = {}",notificationDto.getNotificationType());
    }
}