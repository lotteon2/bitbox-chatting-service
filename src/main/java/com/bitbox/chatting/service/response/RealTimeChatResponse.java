package com.bitbox.chatting.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeChatResponse {
    private long chatRoomId; // 채팅방 번호
    private long chatId; // 채팅 번호
    private String message; // 메시지
    private String transmitterId; // 전송자
    private boolean hasSubscription; //구독권(본인이 송신자랑 다를때만 의미있음)

    public static RealTimeChatResponse convertChatToRealTimeChatResponse(long chatRoomId, long chatId, String message, String transmitterId, boolean hasSubscription){
        return RealTimeChatResponse.builder()
                .chatRoomId(chatRoomId)
                .chatId(chatId)
                .message(message)
                .transmitterId(transmitterId)
                .hasSubscription(hasSubscription)
                .build();
    }
}
