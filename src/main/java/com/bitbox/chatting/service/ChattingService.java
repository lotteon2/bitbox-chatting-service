package com.bitbox.chatting.service;

import com.bitbox.chatting.domain.Chat;
import com.bitbox.chatting.domain.ChatRoom;
import com.bitbox.chatting.dto.ChattingDto;
import com.bitbox.chatting.dto.ChattingRoomDto;
import com.bitbox.chatting.dto.SubscriptionServerInfoDto;
import com.bitbox.chatting.exception.BadRequestException;
import com.bitbox.chatting.exception.DuplicationRoomException;
import com.bitbox.chatting.exception.PaymentFailException;
import com.bitbox.chatting.repository.ChatRepository;
import com.bitbox.chatting.repository.ChatRoomRepository;
import com.bitbox.chatting.repository.response.RoomMessage;
import com.bitbox.chatting.service.response.*;
import io.github.bitbox.bitbox.dto.MemberCreditDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChattingService {
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final KafkaTemplate<String, RealTimeChatResponse> realTimeChatResponseKafkaTemplate;
    private final KafkaTemplate<String, ChatRoomCreationEventResponse> creationKafkaTemplate;
    private final KafkaTemplate<String, MemberCreditDto> memberCreditDtoKafkaTemplate;
    @Value("${chatTopicName}")
    private String chatTopicName;
    @Value("${chatCreationTopicName}")
    private String chatCreationTopicName;
    @Value("${userCreditModifyTopicName}")
    private String userCreditModifyTopicName;

    private final String guest = "guest";
    private final int defultPlusCredit = 1;

    public ConnectionResponse getConnectionListWithUnreadMessage(String memberId){
        List<Long> chatRoomIds = chatRoomRepository.findChatRoomIdsByGuestIdOrHostId(memberId);
        Long unReadMessageCount = chatRepository.getUnreadMessageCountForRooms(chatRoomIds, memberId);

        ConnectionResponse connectionResponse = new ConnectionResponse();
        connectionResponse.setRooms(chatRoomIds);
        connectionResponse.setUnReadMessageCount(unReadMessageCount);
        return connectionResponse;
    }

    public RoomListResponse getChattingRoomList(SubscriptionServerInfoDto subscriptionServerInfoDto, String memberId){
        RoomListResponse roomListResponseList = new RoomListResponse();

        List<RoomMessage> roomListWithLatestMessage = chatRepository.getRoomListWithLatestMessage(memberId);

        for(RoomMessage roomMessage : roomListWithLatestMessage){
            RoomList roomList = RoomList.convertRoomMessageToRoomList(roomMessage);
            if(isSecret(roomMessage, memberId, subscriptionServerInfoDto.isHasSubscription())) {
                roomList.setLatestMessage("");
                roomList.setSecret(true);
            }
            roomListResponseList.getRoomList().add(roomList);
        }

        roomListResponseList.setMessage(subscriptionServerInfoDto.getMessage());
        return roomListResponseList;
    }

    @Transactional
    public ChatRoom createChatRoom(ChattingRoomDto chattingRoomDto){
        if(chattingRoomDto.getGuestId().equals(chattingRoomDto.getHostId())){
            throw new BadRequestException("잘못된 요청입니다.");
        }
        chatRoomRepository.findByGuestIdAndHostId(chattingRoomDto.getGuestId(), chattingRoomDto.getHostId()).ifPresent(chatRoom ->{ throw new DuplicationRoomException("이미 방이 존재합니다");});
        ChatRoom returnChatRoom  = chatRoomRepository.save(ChatRoom.convertChattingRoomDtoToChatRoom(chattingRoomDto));

        creationKafkaTemplate.send(chatCreationTopicName, ChatRoomCreationEventResponse.builder()
                .hostId(returnChatRoom.getHostId())
                .guestId(returnChatRoom.getGuestId())
                .chatRoomId(returnChatRoom.getChatRoomId())
                .build());
        return returnChatRoom;
    }

    @Transactional
    public String payMessage(String memberId, long chatId) {
        // TODO 이거 생각해봐야됨.
        Chat chat = null;
        try {
            chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> {
                        memberCreditDtoKafkaTemplate.send(userCreditModifyTopicName, MemberCreditDto.builder().memberId(memberId).credit(defultPlusCredit).build());
                        throw new PaymentFailException("메시지 정보를 찾을 수 없습니다");
                    });

            chatRepository.updateIsPaid(chatId);
        }catch(PaymentFailException e){
            throw e;
        }catch(Exception e){
            memberCreditDtoKafkaTemplate.send(userCreditModifyTopicName, MemberCreditDto.builder().memberId(memberId).credit(defultPlusCredit).build());
        }
        return chat.getChatContent();
    }

    @Transactional
    public List<ChatResponse> getChatting(String memberId, SubscriptionServerInfoDto subscriptionServerInfoDto, long chatRoomId) {
        // 해당 채팅방을 조회해온다
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new BadRequestException("잘못된 요청입니다."));
        if (!chatRoom.getGuestId().equals(memberId) && !chatRoom.getHostId().equals(memberId)) {
            throw new BadRequestException("잘못된 요청입니다.");
        }

        // 해당 채팅방에서 파라미터로 날라온 member가 guest이거나 구독권이 존재여부 설정
        boolean secretFlag = !(subscriptionServerInfoDto.isHasSubscription() || memberId.equals(chatRoom.getGuestId()));
        // 해당 채팅방의 메시지 읽음여부를 true로 설정한다
        chatRepository.updateMessageReadFlagByRoomIdAndMemberId(chatRoomId, memberId);

        List<ChatResponse> chatResponseList = chatRepository.findChatResponsesByChatRoomId(chatRoomId, memberId, secretFlag);
        if (chatResponseList == null) {
            throw new BadRequestException("잘못된 요청입니다.");
        }

        return chatResponseList;
    }

    @Transactional
    public void updateIsReadByChatId(Long chatId, String memberId){
        chatRepository.updateIsReadByChatId(chatId, memberId);
    }

    @Transactional
    public void createChat(boolean hasSubscription, ChattingDto chattingDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(chattingDto.getChatRoomId()).orElseThrow(() -> new BadRequestException("잘못된 요청입니다."));
        Chat chat = chatRepository.save(Chat.createChat(chatRoom, chattingDto.getTransmitterId(), chattingDto.getChatContent()));
        realTimeChatResponseKafkaTemplate.send(chatTopicName, RealTimeChatResponse.convertChatToRealTimeChatResponse(chattingDto.getChatRoomId(),
                chat.getChatId(), chat.getChatContent(), chat.getTransmitterId(), hasSubscription));
    }

    private boolean isSecret(RoomMessage roomMessage, String memberId, boolean hasSubscription){ // 메시지를 숨겨야하는가 여부 판단
        // 구독권이 있는 케이스
        // 결제여부가 있는 케이스
        // 보낸사람과 memberId가 일치하는 케이스
        // 내가 Host인 케이스
        if(roomMessage.getLatestMessageIsPaid() == null || hasSubscription || roomMessage.getLatestMessageIsPaid()
                || roomMessage.getLatestMessageSender().equals(memberId) || roomMessage.getRole().equals(guest)){
            return false;
        }

        return true;
    }
}