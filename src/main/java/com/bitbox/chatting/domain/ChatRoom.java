package com.bitbox.chatting.domain;


import com.bitbox.chatting.dto.ChattingRoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="chat_room", uniqueConstraints = {@UniqueConstraint(columnNames = {"host_id", "guest_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_room_id")
    private Long chatRoomId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Chat> chats;

    @Column(name="host_id", nullable = false)
    private String hostId;

    @Column(name="host_name", nullable = false)
    private String hostName;

    @Column(name="guest_id", nullable = false)
    private String guestId;

    @Column(name="guest_name", nullable = false)
    private String guestName;

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public static ChatRoom convertChattingRoomDtoToChatRoom(ChattingRoomDto chattingRoomDto){
        return ChatRoom.builder()
                .hostId(chattingRoomDto.getHostId())
                .hostName(chattingRoomDto.getHostName())
                .guestId(chattingRoomDto.getGuestId())
                .guestName(chattingRoomDto.getGuestName())
                .build();
    }
}