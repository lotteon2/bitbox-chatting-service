package com.bitbox.chatting.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="chat_room", uniqueConstraints = {@UniqueConstraint(columnNames = {"host_id", "guest_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
public class ChatRoom extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_room_id")
    private Long chatRoomId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Chat> chats;

    @Getter
    @Column(name="host_id", nullable = false)
    private String hostId;

    @Column(name="host_name", nullable = false)
    private String hostName;

    @ColumnDefault("'https://mblogthumb-phinf.pstatic.net/MjAyMDExMDFfMTgy/MDAxNjA0MjI4ODc1NDMw.Ex906Mv9nnPEZGCh4SREknadZvzMO8LyDzGOHMKPdwAg.ZAmE6pU5lhEdeOUsPdxg8-gOuZrq_ipJ5VhqaViubI4g.JPEG.gambasg/%EC%9C%A0%ED%8A%9C%EB%B8%8C_%EA%B8%B0%EB%B3%B8%ED%94%84%EB%A1%9C%ED%95%84_%ED%95%98%EB%8A%98%EC%83%89.jpg?type=w800'")
    @Column(name = "host_profile_img", nullable = false, columnDefinition = "VARCHAR(300)")
    private String hostProfileImg;

    @Getter
    @Column(name="guest_id", nullable = false)
    private String guestId;

    @Column(name="guest_name", nullable = false)
    private String guestName;

    @ColumnDefault("'https://mblogthumb-phinf.pstatic.net/MjAyMDExMDFfMTgy/MDAxNjA0MjI4ODc1NDMw.Ex906Mv9nnPEZGCh4SREknadZvzMO8LyDzGOHMKPdwAg.ZAmE6pU5lhEdeOUsPdxg8-gOuZrq_ipJ5VhqaViubI4g.JPEG.gambasg/%EC%9C%A0%ED%8A%9C%EB%B8%8C_%EA%B8%B0%EB%B3%B8%ED%94%84%EB%A1%9C%ED%95%84_%ED%95%98%EB%8A%98%EC%83%89.jpg?type=w800'")
    @Column(name = "guest_profile_img", nullable = false, columnDefinition = "VARCHAR(300)")
    private String guestProfileImg;

    public Long getChatRoomId() {
        return chatRoomId;
    }
}