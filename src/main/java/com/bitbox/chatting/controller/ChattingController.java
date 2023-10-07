package com.bitbox.chatting.controller;

import com.bitbox.chatting.domain.ChatRoom;
import com.bitbox.chatting.dto.ChattingRoomDto;
import com.bitbox.chatting.dto.SubscriptionResponseDto;
import com.bitbox.chatting.dto.SubscriptionServerInfoDto;
import com.bitbox.chatting.exception.PaymentFailException;
import com.bitbox.chatting.feign.FeignServiceClient;
import com.bitbox.chatting.service.ChattingService;
import com.bitbox.chatting.service.response.ChatResponse;
import com.bitbox.chatting.service.response.ConnectionResponse;
import com.bitbox.chatting.service.response.RoomListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/")
@RestController
@RequiredArgsConstructor
public class ChattingController {
    private final ChattingService chattingService;
    private final FeignServiceClient feignServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private String headerMemberId = "csh"; // TODO HEADER값으로 변경 필요
    private String headerMemberName = "최성훈";
    private final int defultMinusCredit = 1;

    @GetMapping("connection/list")
    public ResponseEntity<ConnectionResponse> connectionList(){
        return ResponseEntity.ok(chattingService.getConnectionListWithUnreadMessage(headerMemberId));
    }

    @GetMapping("chatting-room")
    public ResponseEntity<RoomListResponse> chattingRoomList(){
        return ResponseEntity.ok(chattingService.getChattingRoomList(getSubscription(), headerMemberId));
    }

    @GetMapping("/chatting-room/{roomId}")
    public List<ChatResponse> chatting(@PathVariable long roomId){
        return chattingService.getChatting(headerMemberId, getSubscription(), roomId);
    }

    @PostMapping("chatting-room")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChattingRoomDto chattingRoomDto){
        chattingRoomDto.setHostId(headerMemberId);
        chattingRoomDto.setHostName(headerMemberName);
        return ResponseEntity.ok(chattingService.createChatRoom(chattingRoomDto));
    }

    @PostMapping("message/{messageId}")
    public ResponseEntity<String> payMessage(@PathVariable long messageId){
        if(!feignServiceClient.updateMemberCredit(-defultMinusCredit).getStatusCode().equals(HttpStatus.OK)){
            throw new PaymentFailException("결제 실패 했습니다.");
        }
        return ResponseEntity.ok(chattingService.payMessage(headerMemberId, messageId));
    }

    @PatchMapping("/chatting-room/{chatRoomId}/messages/{messageId}/mark-as-read")
    public ResponseEntity<Void> updateIsRead(@PathVariable long messageId){
        chattingService.updateIsReadByChatId(messageId);
        return ResponseEntity.ok().build();
    }

    private SubscriptionServerInfoDto getSubscription() {
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        SubscriptionResponseDto subscriptionResponseDto = circuitbreaker.run(feignServiceClient::getSubscription, throwable -> null);
        SubscriptionServerInfoDto subscriptionServerInfoDto = new SubscriptionServerInfoDto();

        if (subscriptionResponseDto == null) { // 구독권 서버를 확인할 수 없는 경우
            subscriptionServerInfoDto.setMessage("구독권 정보를 확인할 수 없습니다.");
        }

        subscriptionServerInfoDto.setHasSubscription(subscriptionResponseDto != null && subscriptionResponseDto.isValid());

        return subscriptionServerInfoDto;
    }
}

/*
    특정 채팅방 조회 -> 특정 채팅방의 내용들을 모두 다 가지고 온다.
    -> 특정 채팅방을 클릭하면 우선, 헤더로 날라온 값에 해당하는 사람이
    HOST(질문자)인지 GUEST(답변자)인지 체크 답변자의 경우 모든 메시지를 전체 공개한다
    그리고 해당 채팅방의 IS_READ(TRANSMITTER_ID가 본인이 아닌)를 true로 설정한다.
    -> 질문자의 경우 TRANSMITTER_ID가 본인이 아닌데 IS_PAID가 false인 경우 메시지를 숨긴다.
    (단! 구독권 모듈에 구독권 정보를 조회 후 존재하면 메시지를 숨기지 않는다 -> 서킷 브레이커)
    그리고 해당 채팅방의 IS_READ(TRANSMITTER_ID가 본인이 아닌)를 true로 설정한다
    -> /chatting/rooms/{roomId}

    특정 유저가 구독을 하고 있고 메시지 발생시 카프카에 저장 후 이게 해당 방을 구독하는 애들에게 메시지를 쏜다
    근데 받아줄 때 해당 창이 열려있고(변수 제어 가능) 내가보고 있는거랑 그게 일치하면 메시지를 그리고
    아니면 안읽은 메시지 개수를 더하는 개념?

    채팅방 생성하면 반대쪽도 뭔가 받아줘야하니 폴링의 개념?

    /1
    /2 -> 보고있어 -> 알림옴 -> 메시지 그림 -> 읽음처리
    /3 -> 알림옴 -> 안읽은메시지1
    /4
    /5
    /6
    /7
    /8
 */