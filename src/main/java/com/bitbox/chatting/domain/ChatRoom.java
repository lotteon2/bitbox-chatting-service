package com.bitbox.chatting.domain;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="chat_room", uniqueConstraints = {@UniqueConstraint(columnNames = {"host_id", "guest_id"})})
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_room_id")
    private Long charRoomId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Chat> chats;

    @Column(name="host_id", nullable = false)
    private String hostId;

    @Column(name="guest_id", nullable = false)
    private String guestId;
}
