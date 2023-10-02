package com.bitbox.chatting.service.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;

@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RoomList {
    private Long chatRoomId;
    private String otherPerson;
    private String latestMessage;
    private boolean isSecret; // 해당 메시지가 결제가 안되어있는가?

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }
}