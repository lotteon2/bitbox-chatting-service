package com.bitbox.chatting.repository;

import com.bitbox.chatting.domain.ChatRoom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends CrudRepository<ChatRoom, Long> {
    @Query("SELECT cr.chatRoomId FROM ChatRoom cr WHERE cr.guestId = :memberId OR cr.hostId = :memberId")
    List<Long> findChatRoomIdsByGuestIdOrHostId(@Param("memberId") String memberId);
}