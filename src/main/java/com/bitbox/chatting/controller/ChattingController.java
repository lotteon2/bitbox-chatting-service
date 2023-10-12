package com.bitbox.chatting.controller;

import com.bitbox.chatting.domain.ChatRoom;
import com.bitbox.chatting.dto.ChattingDto;
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
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
        return ResponseEntity.ok(chattingService.getChattingRoomList(getSubscription(null), headerMemberId));
    }

    @GetMapping("chatting-room/{roomId}")
    public ResponseEntity<List<ChatResponse>> chatting(@PathVariable long roomId){
        return ResponseEntity.ok(chattingService.getChatting(headerMemberId, getSubscription(null), roomId));
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

    @PatchMapping("chatting-room/{roomId}/messages/{messageId}/mark-as-read")
    public ResponseEntity<Void> updateIsRead(@PathVariable long messageId){
        chattingService.updateIsReadByChatId(messageId, headerMemberId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("{roomId}")
    public void sendMessageToKafka(@DestinationVariable Long roomId, ChattingDto chattingDto) {
        chattingDto.setTransmitterId(headerMemberId);
        chattingDto.setChatRoomId(roomId);
        chattingService.createChat(getSubscription(chattingDto.getReceiverId()).isHasSubscription(), chattingDto);
    }

    private SubscriptionServerInfoDto getSubscription(String memberId) {
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        SubscriptionResponseDto subscriptionResponseDto;

        if(memberId == null) { // 본인의 구독권 확인
            subscriptionResponseDto = circuitbreaker.run(feignServiceClient::getSubscription, throwable -> null);
        }else{ // 상대방의 구독권 확인
            subscriptionResponseDto = circuitbreaker.run(() -> feignServiceClient.getSubscription(memberId), throwable -> null);
        }

        SubscriptionServerInfoDto subscriptionServerInfoDto = new SubscriptionServerInfoDto();

        if (subscriptionResponseDto == null) { // 구독권 서버를 확인할 수 없는 경우
            subscriptionServerInfoDto.setMessage("구독권 정보를 확인할 수 없습니다.");
        }

        subscriptionServerInfoDto.setHasSubscription(subscriptionResponseDto != null && subscriptionResponseDto.isValid());

        return subscriptionServerInfoDto;
    }
}
