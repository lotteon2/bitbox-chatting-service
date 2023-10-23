package com.bitbox.chatting.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="chat")
@Getter
@DynamicInsert
@NoArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_id")
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "chat_content", nullable = false, columnDefinition = "text")
    private String chatContent;

    @Column(name = "transmitter_id", nullable = false)
    private String transmitterId;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp(6) default current_timestamp(6)")
    private LocalDateTime createdAt;

    @Column(name = "is_paid", nullable = false, columnDefinition = "boolean default false")
    private boolean isPaid;

    @Column(name = "is_read", nullable = false, columnDefinition = "boolean default false")
    private boolean isRead;

    @Builder
    public Chat(ChatRoom chatRoom, String transmitterId, String chatContent, LocalDateTime createdAt) {
        this.chatRoom =  chatRoom;
        this.transmitterId = transmitterId;
        this.chatContent = chatContent;
        this.createdAt = createdAt;
    }
}