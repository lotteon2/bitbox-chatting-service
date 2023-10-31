package com.bitbox.chatting.dto;

import com.bitbox.chatting.domain.ChatRoom;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ChattingRoomDto {
    private String hostId;
    private String hostName;
    private String hostProfileImg;

    @NotEmpty(message = "게스트 아이디는 비어있을 수 없습니다.")
    private String guestId;
    @NotEmpty(message = "게스트 별칭은 비어있을 수 없습니다.")
    private String guestName;
    @NotEmpty(message = "게스트 프로필 비어있을 수 없습니다.")
    private String guestProfileImg;

    public static ChatRoom convertChattingRoomDtoToChatRoom(ChattingRoomDto chattingRoomDto){
        return ChatRoom.builder()
                .hostId(chattingRoomDto.getHostId())
                .hostName(chattingRoomDto.getHostName())
                .hostProfileImg(chattingRoomDto.getHostProfileImg())
                .guestId(chattingRoomDto.getGuestId())
                .guestName(chattingRoomDto.getGuestName())
                .guestProfileImg(chattingRoomDto.getGuestProfileImg())
                .build();
    }
}
