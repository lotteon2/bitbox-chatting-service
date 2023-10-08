package com.bitbox.chatting.repository.response;

public interface RoomMessage {
    Long getChatRoomId(); // 채팅방 번호
    String getLatestMessage(); // 가장 최근의 메시지 1건
    Boolean getLatestMessageIsPaid(); // 해당 메시지의 결제여부
    String getLatestMessageSender(); // 해당 메시지를 보낸사람
    String getRole(); // 해당 채팅방의 Guest여부
    String getOtherUserId(); // 상대방 아이디
    String getOtherUserName(); // 상대방 이름
}