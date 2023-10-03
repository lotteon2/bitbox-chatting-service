package com.bitbox.chatting.dto;

import lombok.Getter;

@Getter
public class ChattingRoomDto {
    private String hostId;
    private String hostName;
    private String guestId;
    private String guestName;

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public ChattingRoomDto(String guestId, String guestName) {
        this.guestId = guestId;
        this.guestName = guestName;
    }
}
