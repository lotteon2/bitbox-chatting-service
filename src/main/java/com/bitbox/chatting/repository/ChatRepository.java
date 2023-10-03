package com.bitbox.chatting.repository;

import com.bitbox.chatting.domain.Chat;
import com.bitbox.chatting.repository.response.RoomMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends CrudRepository<Chat, Long> {
    @Query("SELECT SUM(CASE WHEN c.isRead = false THEN 1 ELSE 0 END) FROM Chat c WHERE c.chatRoom.chatRoomId  IN :chatRoomIds AND c.isRead = false AND c.transmitterId != :memberId")
    Long getUnreadMessageCountForRooms(@Param("chatRoomIds") List<Long> chatRoomIds, @Param("memberId") String memberId);

    @Query(value = "WITH LatestMessages AS ( " +
            "    SELECT " +
            "        cr.chat_room_id, " +
            "        c.chat_content AS latest_message, " +
            "        c.is_paid AS latest_message_is_paid, " +
            "        c.transmitter_id AS latest_message_sender, " +
            "        cr.guest_id AS guest_id, " +
            "        cr.host_id AS host_id, " +
            "        cr.guest_name AS guest_name, " +
            "        cr.host_name AS host_name, " +
            "        ROW_NUMBER() OVER(PARTITION BY cr.chat_room_id ORDER BY c.created_at DESC) AS rn " +
            "    FROM chat c " +
            "    RIGHT JOIN chat_room cr ON c.chat_room_id = cr.chat_room_id " +
            "    WHERE (cr.guest_id = :memberId OR cr.host_id = :memberId) " +
            ") " +
            "SELECT " +
            "    chat_room_id AS chatRoomId, " +
            "    latest_message AS latestMessage, " +
            "    latest_message_is_paid AS latestMessageIsPaid, " +
            "    latest_message_sender AS latestMessageSender, " +
            "    CASE " +
            "        WHEN guest_id = :memberId THEN 'guest' " +
            "        WHEN host_id = :memberId THEN 'host' " +
            "    END AS role, " +
            "    CASE " +
            "        WHEN guest_id = :memberId THEN host_name " +
            "        WHEN host_id = :memberId THEN guest_name " +
            "    END AS otherUserName " +
            "FROM LatestMessages " +
            "WHERE rn = 1 ORDER BY CHAT_ROOM_ID", nativeQuery = true)
    List<RoomMessage> getRoomListWithLatestMessage(@Param("memberId") String memberId);
}