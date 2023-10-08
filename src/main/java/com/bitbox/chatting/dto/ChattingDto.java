package com.bitbox.chatting.dto;

import lombok.Getter;

@Getter
public class ChattingDto {
    private String chatContent;
    private String transmitterId;
    private String receiverId;
    private Long chatRoomId;


    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public void setTransmitterId(String transmitterId) {
        this.transmitterId = transmitterId;
    }
}
