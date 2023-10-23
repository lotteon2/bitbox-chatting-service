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
import com.bitbox.chatting.repository.response.RoomList;
import com.bitbox.chatting.service.response.ChatResponse;
import com.bitbox.chatting.service.response.ConnectionResponse;
import com.bitbox.chatting.service.response.RoomListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@EmbeddedKafka( partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:7777"},
        ports = {7777})
class ChattingServiceTest {
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired ChatRepository chatRepository;
    @Autowired ChattingService chattingService;
    @BeforeEach
    public void before(){
        List<ChatRoom> chatRoomList = new ArrayList<>();
        /*
            최성훈(host), csh
            최국일(guest), cki
         */
        ChatRoom chatRoom1 = ChatRoom.builder().hostId("csh").hostName("최성훈").guestId("cki").guestName("최국일").build();
        chatRoomList.add(chatRoom1);
        /*
             최윤재(host), cyj
             최성훈(guest), csh
         */
        ChatRoom chatRoom2 = ChatRoom.builder().hostId("cyj").hostName("최윤재").guestId("csh").guestName("최성훈").build();
        chatRoomList.add(chatRoom2);
        /*
            최국일(host), cki
            최윤재(guest), cyj
         */
        ChatRoom chatRoom3 = ChatRoom.builder().hostId("cki").hostName("최국일").guestId("cyj").guestName("최윤재").build();
        chatRoomList.add(chatRoom3);
        /*
            박재범(host), takgali
            최성훈(guest), csh
         */
        ChatRoom chatRoom4 = ChatRoom.builder().hostId("takgali").hostName("박재범").guestId("csh").guestName("최성훈").build();
        chatRoomList.add(chatRoom4);

        chatRoomRepository.saveAll(chatRoomList);


        List<Chat> chattingList = new ArrayList<>();
        // 1번 채팅방
        chattingList.add(ChattingDto.createChat(chatRoom1, "csh", "대구는 따뜻하니?"));
        chattingList.add(ChattingDto.createChat(chatRoom1, "cki", "아니 따뜻하지 않는데? 앞에봐 1:0"));
        chattingList.add(ChattingDto.createChat(chatRoom1, "cki", "."));
        chattingList.add(ChattingDto.createChat(chatRoom1, "csh", "그래.."));

        // 2번 채팅방
        chattingList.add(ChattingDto.createChat(chatRoom2, "cyj", "아니 회사에서 자꾸 문서만 시키는데 이게 맞냐?"));
        chattingList.add(ChattingDto.createChat(chatRoom2, "cyj", "하.. 관둘까?"));
        chattingList.add(ChattingDto.createChat(chatRoom2, "cyj", "어케 생각함"));
        chattingList.add(ChattingDto.createChat(chatRoom2, "csh", "근데 너는 그런게 딱인데?"));

        // 3번 채팅방
        chattingList.add(ChattingDto.createChat(chatRoom3, "cyj", "국일아 오랜만에"));
        chattingList.add(ChattingDto.createChat(chatRoom3, "cyj", "얼굴좀 보자"));
        chattingList.add(ChattingDto.createChat(chatRoom3, "cyj", "씹지말고"));
        chattingList.add(ChattingDto.createChat(chatRoom3, "cyj", "응?"));

        // 4번 채팅방
        chattingList.add(ChattingDto.createChat(chatRoom4, "csh", ".?"));
        chatRepository.saveAll(chattingList);
    }

    @Test
    public void 로그인시_csh계정은_3개의_연결리스트_정보와_안읽은_메시지_수가_7개여야한다(){
        connectionListTest("csh", 3, 5);
    }

    @Test
    public void 메시지_전체를_읽음처리하면_cki계정은_1개의_연결리스트_정보와_안읽은_메시지_수가_0개여야한다(){
        Long chattingRoomNumber = null;

        // 아래의 행위는 테스트에서만 사용하므로 해당 동작을 query 만들지 않았음
        for(ChatRoom chatRoom : chatRoomRepository.findAll()){
            if("csh".equals(chatRoom.getGuestId()) && "takgali".equals(chatRoom.getHostId())){
                chattingRoomNumber = chatRoom.getChatRoomId();
                break;
            }
        }

        chatRepository.updateMessageReadFlagByRoomIdAndMemberId(chattingRoomNumber, "takgali");
        connectionListTest("takgali", 1, 0);
    }

    @Test
    public void 구독권_정보가_있는경우_cyj의_최근메시지의_secret수는_0이다(){
        secretMessageCountTest(getSubscription(true), "cyj", 0 );
    }

    @Test
    public void 구독권_정보가_없는데_cyj가_해당방의_guest라면_cyj의_secret메시지의_수는_0이다(){
        secretMessageCountTest(getSubscription(false), "cyj", 0 );
    }

    @Test
    public void 구독권_정보가_없고_마지막메시지를_보낸사람이_아니고_host라면_takgali의_최근1개채팅내역의_secret메시지의_수는_1이다(){
        secretMessageCountTest(getSubscription(false), "takgali", 1 );
    }

    @Test
    public void 구독권_정보가_있고_마지막메시지를_보낸사람이_아니고_host라면_takgali의_최근1개채팅내역의_secret메시지의_수는_0이다(){
        secretMessageCountTest(getSubscription(true), "takgali", 0 );
    }

    @Test
    public void 채팅방이_정상적으로_만들어진다(){
        chatRoomRepository.findById(chattingCreationTest("csh","최성훈","sji","손정인"));
    }

    @Test
    public void 이미_존재하는_채팅방은_만들어지지_않는다(){
        assertThrows(DuplicationRoomException.class, () -> chattingCreationTest("csh","최성훈","cki","최국일"));
    }

    @Test
    public void 게스트아이디랑_호스트아이디가_같으면_BadRequestException이_발생한다(){
        assertThrows(BadRequestException.class, () -> chattingCreationTest("csh","최성훈","csh","최성훈"));
    }

    @Test
    public void 존재하지않는_메시지_번호이므로_PaymentFailException가_발생한다(){
        assertThrows(PaymentFailException.class, ()->chattingService.payMessage("csh", 1000L));
    }

    @Test
    public void 존재하는_메시지_번호이므로_해당_메시지_내역을_확인할수있다(){
        List<ChatResponse> response = chatRepository.findChatResponsesByChatRoomId(chatRoomRepository.findChatRoomIdsByGuestIdOrHostId("takgali").get(0),
                "takgali", false);
        assertEquals(chattingService.payMessage("takgali",response.get(0).getChatId()), response.get(0).getMessage());
    }

    @Test
    public void csh가_속한_모든채팅방의_모든_메시지는_9개이고_구독권이_없다면_secret_메시지수는_2개다(){
        secretChattingCountTest("csh", getSubscription(false), 2);
    }

    @Test
    public void csh가_속한_모든채팅방의_모든_메시지는_9개이고_구독권이_있다면_secret_메시지수는_0개다(){
        SubscriptionServerInfoDto subscriptionServerInfoDto = new SubscriptionServerInfoDto();
        subscriptionServerInfoDto.setHasSubscription(true);
        secretChattingCountTest("csh", subscriptionServerInfoDto, 0);
    }

    @Test
    public void cyj가_속한_채팅방이_아닌데_해당_채팅방의_채팅내역을_요구하는경우_BadRequestException이_발생한다(){
        List<Long> rooms = chatRoomRepository.findChatRoomIdsByGuestIdOrHostId("takgali");
        assertThrows(BadRequestException.class, ()->chattingService.getChatting("cyj",getSubscription(true),rooms.get(0)));
    }

    @Test
    public void takgali_가속한_채팅방의_안읽은메시지_개수는_1이고_takgali가_읽음여부를_변경하면_업데이트_로우는_1개이다(){
        isReadUpdateTest("takgali","takgali",1,1);
    }

    @Test
    public void takgali_가속한_채팅방의_안읽은메시지_개수는_1이고_csh가_읽음여부를_변경하면_업데이트_로우는_0개이다(){
        isReadUpdateTest("takgali","csh",1,0);
    }

    @Test
    public void 채팅방이_없으면_BadRequestException_예외가_발생한다(){
        assertThrows(BadRequestException.class, ()->chattingService.createChat(getSubscription(false).isHasSubscription(), new ChattingDto("hi","csh","csh1",11L)));
    }

    @Test
    public void 정상적으로_채팅방에_채팅이_저장된다(){
        List<Long> rooms = chatRoomRepository.findChatRoomIdsByGuestIdOrHostId("takgali");
        Chat chat = chattingService.createChat(getSubscription(false).isHasSubscription(), new ChattingDto("hi", "takgali", "csh", rooms.get(0)));
        assertEquals(chatRepository.findById(chat.getChatId()).isPresent(), true);
    }

    private void connectionListTest(String memberId, int expectedRoomSize, int expectedUnReadMessageCount) {
        ConnectionResponse connectionResponse = chattingService.getConnectionListWithUnreadMessage(memberId);
        assertEquals(connectionResponse.getRooms().size(), expectedRoomSize);
        assertEquals(connectionResponse.getUnReadMessageCount(), expectedUnReadMessageCount);
    }

    private void secretMessageCountTest(SubscriptionServerInfoDto subscriptionServerInfoDto, String memberId, int expected){
        RoomListResponse roomListResponse = chattingService.getChattingRoomList(subscriptionServerInfoDto, memberId);
        int count = 0;
        for(RoomList roomList : roomListResponse.getRoomList()){
            if(roomList.getIsSecret() == 1L){
                count++;
            }
        }
        assertEquals(count, expected);
    }

    private Long chattingCreationTest(String hostId, String hostName, String guestId, String guestName){
        ChattingRoomDto chattingRoomDto = new ChattingRoomDto();
        chattingRoomDto.setHostId(hostId);
        chattingRoomDto.setHostName(hostName);
        chattingRoomDto.setGuestId(guestId);
        chattingRoomDto.setGuestName(guestName);

        return chattingService.createChatRoom(chattingRoomDto).getChatRoomId();
    }

    private void secretChattingCountTest(String memberId, SubscriptionServerInfoDto subscriptionServerInfoDto, int expected){
        List<Long> rooms = chatRoomRepository.findChatRoomIdsByGuestIdOrHostId(memberId);

        long sum = rooms.stream()
                .flatMap(roomId -> chattingService.getChatting(memberId, subscriptionServerInfoDto, roomId).stream())
                .filter(ChatResponse::isSecret)
                .count();
        assertEquals(sum,expected);
    }

    private SubscriptionServerInfoDto getSubscription(boolean flag){
        SubscriptionServerInfoDto subscriptionServerInfoDto = new SubscriptionServerInfoDto();
        subscriptionServerInfoDto.setHasSubscription(flag);

        return subscriptionServerInfoDto;
    }

    private void isReadUpdateTest(String memberId1, String memberId2, int expected1, int expected2){
        List<Long> rooms = chatRoomRepository.findChatRoomIdsByGuestIdOrHostId(memberId1);

        List<Chat> chattingList = chatRepository.findChatByChatRoomId(rooms.get(0));
        assertEquals(chattingList.stream()
                .filter(chat -> !chat.isRead())
                .count(), expected1);

        assertEquals(chattingService.updateIsReadByChatId(chattingList.get(0).getChatId(), memberId2), expected2);
    }
}