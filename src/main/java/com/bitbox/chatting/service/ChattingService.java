package com.bitbox.chatting.service;

import com.bitbox.chatting.domain.ChatRoom;
import com.bitbox.chatting.dto.ChattingRoomDto;
import com.bitbox.chatting.dto.SubscriptionResponse;
import com.bitbox.chatting.exception.BadRequestException;
import com.bitbox.chatting.exception.DuplicationRoomException;
import com.bitbox.chatting.feign.FeignServiceClient;
import com.bitbox.chatting.repository.ChatRepository;
import com.bitbox.chatting.repository.ChatRoomRepository;
import com.bitbox.chatting.repository.response.RoomMessage;
import com.bitbox.chatting.service.response.ConnectionResponse;
import com.bitbox.chatting.service.response.RoomList;
import com.bitbox.chatting.service.response.RoomListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingService {
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FeignServiceClient feignServiceClient;
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
            RoomList roomList = RoomList.convertRoomMessageToRoomList(roomMessage);
            if(isSecret(roomMessage, memberId, hasSubscription)) {
                roomList.setLatestMessage("");
                roomList.setSecret(true);
            }
            roomListResponseList.getRoomList().add(roomList);
        }

        return roomListResponseList;
    }

    public ChatRoom createChatRoom(ChattingRoomDto chattingRoomDto){
        if(chattingRoomDto.getGuestId().equals(chattingRoomDto.getHostId())){
            log.error("header 조작 가능성 있음");
            throw new BadRequestException("잘못된 요청입니다.");
        }
        chatRoomRepository.findByGuestIdAndHostId(chattingRoomDto.getGuestId(), chattingRoomDto.getHostId()).ifPresent(chatRoom ->{ throw new DuplicationRoomException("이미 방이 존재합니다");});
        return chatRoomRepository.save(ChatRoom.convertChattingRoomDtoToChatRoom(chattingRoomDto));
    }

    public String payMessage(){


        return "hello world";
    }

    private boolean getSubscription(RoomListResponse roomListResponse) {
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        SubscriptionResponse subscriptionResponse = circuitbreaker.run(feignServiceClient::getSubscription, throwable -> null);

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
        if(roomMessage.getLatestMessageIsPaid() == null || hasSubscription || roomMessage.getLatestMessageIsPaid() || roomMessage.getLatestMessageSender().equals(memberId) || roomMessage.getRole().equals(guest)){
            return false;
        }

        return true;
    }

}