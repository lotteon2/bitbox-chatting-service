package com.bitbox.chatting.dto;

import com.bitbox.chatting.domain.Chat;
import com.bitbox.chatting.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChattingDto {
    @NotEmpty(message = "채팅 메시지는 비어있을 수 없습니다.")
    private String chatContent;
    @NotEmpty(message = "전송자 아이디는 비어있을 수 없습니다.")
    private String transmitterId;
    @NotEmpty(message = "수신자 아이디는 비어있을 수 없습니다.")
    private String receiverId;
    private Long chatRoomId;


    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public void setTransmitterId(String transmitterId) {
        this.transmitterId = transmitterId;
    }

    public static Chat createChat(ChatRoom chatRoom, String transmitterId, String chatContent) {
        return Chat.builder()
                .chatRoom(chatRoom)
                .transmitterId(transmitterId)
                .chatContent(chatContent)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
