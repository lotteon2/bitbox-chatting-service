package com.bitbox.chatting.service.response;

import com.bitbox.chatting.repository.response.RoomMessage;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;

@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RoomList {
    private Long chatRoomId;
    private String otherPersonId;
    private String otherPersonName;
    private String latestMessage;
    private boolean isSecret; // 해당 메시지가 결제가 안되어있는가?

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }

    public static RoomList convertRoomMessageToRoomList(RoomMessage roomMessage){
        return RoomList.builder()
                .chatRoomId(roomMessage.getChatRoomId())
                .otherPersonId(roomMessage.getOtherUserId())
                .otherPersonName(roomMessage.getOtherUserName())
                .latestMessage(roomMessage.getLatestMessage())
                .isSecret(false)
                .build();
    }
}