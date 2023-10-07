package com.bitbox.chatting.service.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatResponse {
    private String location; // L, R
    private String message; // 유저와 나눈 메시지
    private boolean isSecret; // 비공개 여부
    private long chatId; // 채팅 아이디
}