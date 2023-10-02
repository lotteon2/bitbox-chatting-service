package com.bitbox.chatting.service;

import com.bitbox.chatting.dto.SubscriptionResponse;
import com.bitbox.chatting.feign.PaymentServiceClient;
import com.bitbox.chatting.repository.ChatRepository;
import com.bitbox.chatting.repository.ChatRoomRepository;
import com.bitbox.chatting.service.response.ConnectionResponse;
import com.bitbox.chatting.service.response.RoomList;
import com.bitbox.chatting.service.response.RoomListResponse;
import com.bitbox.chatting.repository.response.RoomMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChattingService {
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    private final String guest = "guest";

    public ConnectionResponse getConnectionListWithUnreadMessage(String memberId){
        List<Long> chatRoomIds = chatRoomRepository.findChatRoomIdsByGuestIdOrHostId(memberId);
        Long unReadMessageCount = chatRepository.getUnreadMessageCountForRooms(chatRoomIds, memberId);

        ConnectionResponse connectionResponse = new ConnectionResponse();
        connectionResponse.setRooms(chatRoomIds);
        connectionResponse.setUnReadMessageCount(unReadMessageCount);
        return connectionResponse;
    }

    public RoomListResponse getChattingRoomList(String memberId){
        RoomListResponse roomListResponseList = new RoomListResponse();
        boolean hasSubscription = getSubscription(roomListResponseList);

        for(RoomMessage roomMessage : chatRepository.getRoomListWithLatestMessage(memberId)){
            RoomList roomList = RoomList.builder()
                    .chatRoomId(roomMessage.getChatRoomId())
                    .otherPerson(roomMessage.getOtherUserName())
                    .latestMessage(roomMessage.getLatestMessage())
                    .isSecret(false)
                    .build();
            if(isSecret(roomMessage, memberId, hasSubscription)) {
                roomList.setLatestMessage("");
                roomList.setSecret(true);
            }
            roomListResponseList.getRoomList().add(roomList);
        }

        return roomListResponseList;
    }

    private boolean getSubscription(RoomListResponse roomListResponse) {
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        SubscriptionResponse subscriptionResponse = circuitbreaker.run(paymentServiceClient::getSubscription, throwable -> null);

        if (subscriptionResponse == null) { // 구독권 서버를 확인할 수 없는 경우
            roomListResponse.setMessage("구독권 정보를 확인할 수 없습니다.");
        }

        return subscriptionResponse != null && subscriptionResponse.isValid();
    }

    private boolean isSecret(RoomMessage roomMessage, String memberId, boolean hasSubscription){ // 메시지를 숨겨야하는가 여부 판단
        // 구독권이 있는 케이스
        // 결제여부가 있는 케이스
        // 보낸사람과 memberId가 일치하는 케이스
        // 내가 Host인 케이스

        if(hasSubscription || roomMessage.getLatestMessageIsPaid() || roomMessage.getLatestMessageSender().equals(memberId) || roomMessage.getRole().equals(guest)){
            return false;
        }

        return true;
    }

}

// 로그인시만 구독하면 답이없는데
// 누가 채팅방을 팠어 그러면 상대방도 그걸 구독하게 만든다고?
// 이런 미친 로직을 누가 생각? 진짜 주기적으로 폴링해야할듯 ㅋㅋ;