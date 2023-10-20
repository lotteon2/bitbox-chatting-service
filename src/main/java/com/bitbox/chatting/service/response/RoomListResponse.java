package com.bitbox.chatting.service.response;

import com.bitbox.chatting.repository.response.RoomList;
import lombok.*;

import java.util.List;

@Getter
@Builder
public class RoomListResponse {
    private List<RoomList> roomList;
//    private String message;
}