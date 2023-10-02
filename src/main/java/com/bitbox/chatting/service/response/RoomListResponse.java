package com.bitbox.chatting.service.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class RoomListResponse {
    private List<RoomList> roomList;
    private String message;

    public RoomListResponse() {
        roomList = new ArrayList<>();
        message = null;
    }
}