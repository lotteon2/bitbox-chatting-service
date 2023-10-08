package com.bitbox.chatting.service.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponse {
    private String location; // L, R
    private String message; // 유저와 나눈 메시지
    private boolean isSecret; // 비공개 여부
    private long chatId; // 채팅 아이디

    public ChatResponse(String location, String message, boolean isSecret, long chatId) {
        this.location = location;
        this.message = message;
        this.isSecret = isSecret;
        this.chatId = chatId;
    }
}