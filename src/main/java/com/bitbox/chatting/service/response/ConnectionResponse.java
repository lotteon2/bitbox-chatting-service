package com.bitbox.chatting.service.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ConnectionResponse {
    private List<Map<String, Long>> rooms;
    private long unReadMessageCount;

    public void setRooms(List<Long> rooms) {
        List<Map<String, Long>> roomList = new ArrayList<>();
        for (Long roomId : rooms) {
            Map<String, Long> roomMap = new HashMap<>();
            roomMap.put("roomId", roomId);
            roomList.add(roomMap);
        }
        this.rooms = roomList;
    }

    public void setUnReadMessageCount(Long unReadMessageCount) {
        Long savedData = unReadMessageCount;
        unReadMessageCount = savedData == null? 0 : savedData;
        this.unReadMessageCount = unReadMessageCount;
    }
}
