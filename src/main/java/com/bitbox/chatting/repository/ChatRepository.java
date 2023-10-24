package com.bitbox.chatting.repository;

import com.bitbox.chatting.domain.Chat;
import com.bitbox.chatting.service.response.ChatResponse;
import com.bitbox.chatting.repository.response.RoomList;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends CrudRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c WHERE c.chatRoom.chatRoomId = :chatRoomId")
    List<Chat> findChatByChatRoomId(Long chatRoomId);

    @Query("SELECT SUM(CASE WHEN c.isRead = false THEN 1 ELSE 0 END) FROM Chat c WHERE c.chatRoom.chatRoomId  IN :chatRoomIds AND c.isRead = false AND c.transmitterId != :memberId")
    Long getUnreadMessageCountForRooms(@Param("chatRoomIds") List<Long> chatRoomIds, @Param("memberId") String memberId);

    // [TODO] NATIVE QUERY이므로 주의
    @Query(value = "WITH LatestMessages AS ("
            + "SELECT"
            + "    cr.chat_room_id,"
            + "    c.chat_content AS latest_message,"
            + "    c.is_paid AS latest_message_is_paid,"
            + "    c.transmitter_id AS latest_message_sender,"
            + "    cr.guest_id AS guest_id,"
            + "    cr.host_id AS host_id,"
            + "    cr.guest_name AS guest_name,"
            + "    cr.host_name AS host_name,"
            + "    cr.guest_profile_img AS guest_profile_img,"
            + "    cr.host_profile_img AS host_profile_img,"
            + "    ROW_NUMBER() OVER(PARTITION BY cr.chat_room_id ORDER BY c.created_at DESC) AS rn "
            + "FROM chat c "
            + "RIGHT JOIN chat_room cr ON c.chat_room_id = cr.chat_room_id "
            + "WHERE (cr.guest_id = :memberId OR cr.host_id = :memberId)) "
            + "SELECT "
            + "    chat_room_id AS chatRoomId,"
            + "    CASE "
            + "        WHEN guest_id = :memberId THEN host_id "
            + "        WHEN host_id = :memberId THEN guest_id "
            + "    END AS otherUserId,"
            + "    CASE "
            + "        WHEN guest_id = :memberId THEN host_name "
            + "        WHEN host_id = :memberId THEN guest_name "
            + "    END AS otherUserName,"
            + "    CASE "
            + "        WHEN guest_id = :memberId THEN host_profile_img "
            + "        WHEN host_id = :memberId THEN guest_profile_img "
            + "    END AS otherUserProfileImg,"
            + "    CASE WHEN latest_message_is_paid = 1 OR guest_id = :memberId OR :secretFlag = true OR latest_message_sender = :memberId OR latest_message IS NULL "
            + "         THEN latest_message "
            + "         ELSE '' "
            + "    END AS latestMessage,"
            + "    CASE WHEN latest_message_is_paid = 1 OR guest_id = :memberId OR :secretFlag = true OR latest_message_sender = :memberId OR latest_message IS NULL "
            + "         THEN 0 "
            + "         ELSE 1 "
            + "    END AS isSecret "
            + "FROM LatestMessages "
            + "WHERE rn = 1 "
            + "ORDER BY chat_room_id", nativeQuery = true)
    List<RoomList> getRoomListWithLatestMessage(@Param("memberId") String memberId, @Param("secretFlag") boolean secretFlag);

    // [TODO] NATIVE QUERY이므로 주의
    @Modifying
    @Query(value = "UPDATE chat " +
            "SET is_paid = true " +
            "WHERE chat_id IN ( " +
            "    SELECT chat_id " +
            "    FROM ( " +
            "        SELECT chat.chat_id " +
            "        FROM chat " +
            "        JOIN chat_room ON chat.chat_room_id = chat_room.chat_room_id " +
            "        WHERE chat_room.host_id = :hostId " +
            "        AND chat.created_at <= :createdAt " +
            "        AND chat.transmitter_id != :hostId " +
            "    ) AS tmp " +
            ")", nativeQuery = true)
    void updateChatIsPaidByHostAndCreatedAt(@Param("hostId") String hostId,
                                            @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query("UPDATE Chat c SET c.isPaid = true WHERE c.chatId = :chatId")
    int updateIsPaid(long chatId);

    @Modifying
    @Query("UPDATE Chat c SET c.isRead = true WHERE c.chatRoom.chatRoomId = :chatRoomId AND c.transmitterId <> :memberId")
    void updateMessageReadFlagByRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId,
                                                  @Param("memberId") String memberId);

    @Query("SELECT new com.bitbox.chatting.service.response.ChatResponse(" +
            "CASE WHEN c.transmitterId = :memberId THEN 'R' ELSE 'L' END, " +
            "CASE WHEN c.isPaid = true OR c.transmitterId = :memberId OR :secretFlag = false " +
            "THEN c.chatContent ELSE '' END, " +
            "CASE WHEN c.isPaid = true OR c.transmitterId = :memberId OR :secretFlag = false " +
            "THEN false ELSE true END,"+
            "c.chatId) " +
            "FROM Chat c " +
            "WHERE c.chatRoom.chatRoomId = :chatRoomId " +
            "ORDER BY c.createdAt")
    List<ChatResponse> findChatResponsesByChatRoomId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("memberId") String memberId,
            @Param("secretFlag") boolean secretFlag
    );

    @Modifying
    @Query("UPDATE Chat c SET c.isRead = true WHERE c.chatId = :chatId AND c.transmitterId <> :memberId")
    int updateIsReadByChatId(@Param("chatId") Long chatId, @Param("memberId") String memberId);
}