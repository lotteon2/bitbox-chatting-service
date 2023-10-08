package com.bitbox.chatting.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatRoomCreationEventResponse {
    private Long chatRoomId; // 채팅방번호
    private String guestId; // 게스트아이디
    private String hostId; // 호스트아이디
}
