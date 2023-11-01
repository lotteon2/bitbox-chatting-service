package com.bitbox.chatting.controller;

import com.bitbox.chatting.domain.ChatRoom;
import com.bitbox.chatting.dto.ChattingDto;
import com.bitbox.chatting.dto.ChattingRoomDto;
import com.bitbox.chatting.dto.SubscriptionResponseDto;
import com.bitbox.chatting.dto.SubscriptionServerInfoDto;
import com.bitbox.chatting.exception.PaymentFailException;
import com.bitbox.chatting.feign.PaymentFeignServiceClient;
import com.bitbox.chatting.feign.UserFeignServiceClient;
import com.bitbox.chatting.service.ChattingService;
import com.bitbox.chatting.service.response.ChatResponse;
import com.bitbox.chatting.service.response.ConnectionResponse;
import com.bitbox.chatting.service.response.RoomListResponse;
import io.github.bitbox.bitbox.dto.MemberCreditDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequestMapping("/")
@RestController
@RequiredArgsConstructor
public class ChattingController {
    private final ChattingService chattingService;
    private final PaymentFeignServiceClient paymentFeignServiceClient;
    private final UserFeignServiceClient userFeignServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final int defultMinusCredit = 1;

    @GetMapping("connection/list")
    public ResponseEntity<ConnectionResponse> connectionList(@RequestHeader String memberId){
        return ResponseEntity.ok(chattingService.getConnectionListWithUnreadMessage(memberId));
    }

    @GetMapping("chatting-room")
    public ResponseEntity<RoomListResponse> chattingRoomList(@RequestHeader String memberId){
        return ResponseEntity.ok(chattingService.getChattingRoomList(getSubscription(memberId), memberId));
    }

    @GetMapping("chatting-room/{roomId}")
    public ResponseEntity<List<ChatResponse>> chatting(@RequestHeader String memberId, @PathVariable long roomId){
        return ResponseEntity.ok(chattingService.getChatting(memberId, getSubscription(memberId), roomId));
    }

    @PostMapping("chatting-room")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestHeader String memberId, @RequestHeader String memberNickname,
                                                   @RequestHeader String memberProfileImg, @Valid @RequestBody ChattingRoomDto chattingRoomDto){
        chattingRoomDto.setHostId(memberId);
        try {
            chattingRoomDto.setHostName(URLDecoder.decode(memberNickname, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        chattingRoomDto.setHostProfileImg(memberProfileImg);

        return ResponseEntity.ok(chattingService.createChatRoom(chattingRoomDto));
    }

    @PostMapping("message/{messageId}")
    public ResponseEntity<String> payMessage(@RequestHeader String memberId, @PathVariable long messageId){
        if(!userFeignServiceClient.updateMemberCredit(MemberCreditDto.builder().credit(defultMinusCredit).memberId(memberId).build()).getStatusCode().equals(HttpStatus.OK)){
            throw new PaymentFailException("결제 실패 했습니다.");
        }
        return ResponseEntity.ok(chattingService.payMessage(memberId, messageId));
    }

    @PatchMapping("messages/{messageId}/mark-as-read")
    public ResponseEntity<Void> updateIsRead(@RequestHeader String memberId, @PathVariable long messageId){
        chattingService.updateIsReadByChatId(messageId, memberId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("{roomId}")
    public void sendMessageToKafka(@DestinationVariable Long roomId, @Valid ChattingDto chattingDto) {
        chattingDto.setChatRoomId(roomId);
        chattingService.createChat(getSubscription(chattingDto.getReceiverId()).isHasSubscription(), chattingDto);
    }

    private SubscriptionServerInfoDto getSubscription(String memberId) {
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        SubscriptionResponseDto subscriptionResponseDto;

        subscriptionResponseDto = circuitbreaker.run(() -> paymentFeignServiceClient.getSubscription(memberId), throwable -> null);

        SubscriptionServerInfoDto subscriptionServerInfoDto = new SubscriptionServerInfoDto();

        if (subscriptionResponseDto == null) { // 구독권 서버를 확인할 수 없는 경우
            subscriptionServerInfoDto.setMessage("구독권 정보를 확인할 수 없습니다.");
        }

        subscriptionServerInfoDto.setHasSubscription(subscriptionResponseDto != null && subscriptionResponseDto.isValid());

        return subscriptionServerInfoDto;
    }
}
