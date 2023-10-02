package com.bitbox.chatting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bitbox.bitbox.dto.MemberPaymentDto;
import io.github.bitbox.bitbox.util.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumeTestService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    //@KafkaListener(topics = "memberCredit")
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

/*
    채팅방 목록 조회(로그인시) -> 최초 요청임
    -> 특정 유저의 본인이 속해있는 채팅방 목록들을 가져온다 /chatting/rooms
    -> 채팅방을 조회하고 무조건 stomp를 연결하고 있어야 한다.(특정 방들을 구독)
    -> 특정 유저의 읽음 여부가 false인 건에 대한 개수를 가지고 와야한다.
    -> 채팅방에서 본인이 속해있는 채팅방을 가지고 오고 이걸 채팅테이블에 IN으로 해서
    TRANSMITTER_ID가 본인이 아닌 건에 대해서 is_read의 sum(count(*))를 구한다.
    [완료]

    채팅방 아이콘 클릭시(회원명 조회 가능)
    -> 특정 유저가 속해있는 채팅방 (HOST_ID, GUEST_ID)을 조회해서 상대방의
    이름과 가장 최근 메시지를 출력하는데 본인이 HOST면 결제여부 구독권 여부에따라 최근 메시지 보임
    처리하고 GUEST면 무조건 보이게 처리
    [완료]

    채팅방 만들기
    -> 채팅방을 만들 수 있다.
    
    특정 채팅방 조회 -> 특정 채팅방의 내용들을 모두 다 가지고 온다.
    -> 특정 채팅방을 클릭하면 우선, 헤더로 날라온 값에 해당하는 사람이
    HOST(질문자)인지 GUEST(답변자)인지 체크 답변자의 경우 모든 메시지를 전체 공개한다
    그리고 해당 채팅방의 IS_READ(TRANSMITTER_ID가 본인이 아닌)를 true로 설정한다.
    -> 질문자의 경우 TRANSMITTER_ID가 본인이 아닌데 IS_PAID가 false인 경우 메시지를 숨긴다.
    (단! 구독권 모듈에 구독권 정보를 조회 후 존재하면 메시지를 숨기지 않는다 -> 서킷 브레이커)
    그리고 해당 채팅방의 IS_READ(TRANSMITTER_ID가 본인이 아닌)를 true로 설정한다
    -> /chatting/rooms/{roomId}

    서킷브레이커 하면 뭔가 값은 떨궈주는데 코드를 다르게 해가지고 뭔가 메시지를 떨궈야 할 것 같은데
    예를들면 내가 구독권이 있는지 없는지 모르지만 구독권 모듈을 호출을 못했으면
    "구독권"관련 서버에서 문제가 발생하여 구독권 정보가 존재해도 확인할 수 없습니다? 이런식?

    알림 로직 -> 카프카가 특정 방에 대해서 내용을 알림을 쏘면 받아주는 쪽에서 대화 창이 열려있으면
    (참고로 DB에 해당 채팅을 저장도 해야함.)
    메시지 갱신만 한다. 그리고 해당 메시지에 대해서 읽음 처리를 해야한다.
    안열려있으면 안읽은 메시지 개수에 +1을 한다

    배치 처리
    -> chat_room에서 host_id가 본인이고 그러한 건에 대해서 CHAT 테이블을 조회를 하고
    CREATED_AT이 카프카에서 넘어온 START_TIME과 END_TIME 사이라면 IS_PAID를 true로 설정

    특정 메시지 결제
    -> 특정 메시지에 대해서 결제할 수 있다. (결제는 1크레딧이 소모된다), 근데 이건 유저의
    크레딧을 먼저 구해와야 하므로 feign client인데 서킷 브레이커 아님 해당 로직이 성공하면
    특정 메시지의 결제여부를 바꾸고 해당 메시지를 리턴한다.
    
    [*] 1. 회원쪽에서 크레딧 관련 카프카 처리 메소드가 존재해야함
    [*] 2. 회원쪽에서 특정 유저의 크레딧 조회 관련 메소드가 존재해야함
    [*] 3. 회원쪽에서 크레딧 관련 처리 메소드가 존재해야함(1)하고 별개존재해야함
    -> 2를 그냥 하나의 메소드로 퉁치고 해야하는게 좋을듯?
 */
