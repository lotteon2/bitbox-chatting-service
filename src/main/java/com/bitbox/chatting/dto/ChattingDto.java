package com.bitbox.chatting.dto;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
public class ChattingDto {
    @NotEmpty(message = "채팅 메시지는 비어있을 수 없습니다.")
    private String chatContent;
    @NotEmpty(message = "전송자 아이디는 비어있을 수 없습니다.")
    @Size(max=16, message = "전송자 아이디는 최대 16자까지 입력 가능합니다.")
    private String transmitterId;
    @NotEmpty(message = "수신자 아이디는 비어있을 수 없습니다.")
    @Size(max=16, message = "수신자 아이디는 최대 16자까지 입력 가능합니다.")
    private String receiverId;
    @NotEmpty(message = "채팅방은 비어있을 수 없습니다.")
    private Long chatRoomId;


    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public void setTransmitterId(String transmitterId) {
        this.transmitterId = transmitterId;
    }
}
