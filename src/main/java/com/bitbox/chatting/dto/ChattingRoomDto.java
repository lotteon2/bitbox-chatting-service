package com.bitbox.chatting.dto;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
public class ChattingRoomDto {
    @NotEmpty(message = "호스트 아이디는 비어있을 수 없습니다.")
    @Size(max=16, message = "호스트 아이디는 최대 16자까지 입력 가능합니다.")
    private String hostId;
    @NotEmpty(message = "호스트 별칭은 비어있을 수 없습니다.")
    @Size(max=100, message = "호스트 별칭은 최대 100까지 입력 가능합니다.")
    private String hostName;
    @NotEmpty(message = "게스트 아이디는 비어있을 수 없습니다.")
    @Size(max=16, message = "게스트 아이디는 최대 16자까지 입력 가능합니다.")
    private String guestId;
    @NotEmpty(message = "게스트 별칭은 비어있을 수 없습니다.")
    @Size(max=100, message = "게스트 별칭은 최대 100까지 입력 가능합니다.")
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
