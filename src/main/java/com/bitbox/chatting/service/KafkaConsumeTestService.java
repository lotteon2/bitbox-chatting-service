package com.bitbox.chatting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bitbox.bitbox.dto.MemberPaymentDto;
import io.github.bitbox.bitbox.util.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumeTestService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "memberCredit")
    public void kafkaTopicTest(String kafkaMessage) {
        ObjectMapper mapper = new ObjectMapper();
        MemberPaymentDto memberPaymentDto;
        try {
            memberPaymentDto = mapper.readValue(kafkaMessage, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

        log.info("memberId = {}",memberPaymentDto.getMemberId());
        log.info("credit = {}",memberPaymentDto.getMemberCredit());
        log.info("tid = {}",memberPaymentDto.getTid());
        log.info("cancelAmount = {}",memberPaymentDto.getCancelAmount());
        log.info("cancelTaxFreeAmount = {}",memberPaymentDto.getCancelTaxFreeAmount());

        KafkaProducer.send(kafkaTemplate, "kakaopayCancel", memberPaymentDto);
    }
}