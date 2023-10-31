package com.bitbox.chatting.repository.response;

public interface RoomList {
    Long getChatRoomId(); // 채팅방 번호
    String getLatestMessage(); // 가장 최근의 메시지 1건
    String getOtherUserId(); // 상대방 아이디
    String getOtherUserName(); // 상대방 이름
    String getHostId(); // 채팅방의 호스트 아이디
    String getGuestId(); // 채팅방의 게스트 아이디
    // TODO boolean type으로 변경하고 싶다.
    long getIsSecret(); // hidden 여부

    String getOtherUserProfileImg(); // 상대방 프로필 이미지 url
}