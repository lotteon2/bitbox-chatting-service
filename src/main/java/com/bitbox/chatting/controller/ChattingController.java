package com.bitbox.chatting.controller;

import com.bitbox.chatting.service.ChattingService;
import com.bitbox.chatting.service.response.ConnectionResponse;
import com.bitbox.chatting.service.response.RoomListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/")
@RestController
@RequiredArgsConstructor
public class ChattingController {
    private final ChattingService chattingService;
    private String headerMemberId = "csh"; // TODO HEADER값으로 변경 필요

    @GetMapping("connection/list")
    public ResponseEntity<ConnectionResponse> connectionList(){
        return ResponseEntity.ok(chattingService.getConnectionListWithUnreadMessage(headerMemberId));
    }

    @GetMapping("chatting/list")
    public ResponseEntity<RoomListResponse> chattingRoomList(){
        return ResponseEntity.ok(chattingService.getChattingRoomList(headerMemberId));
    }

    // 구독권 정보를 가지고온다
    // chat_room 테이블에서 해당 유저가 host_id에 있거나 guest_id에 있는 경우를 가지고옴

    // 내가 GUEST_ID면 HOST_ID를 출력, 내가 HOST_ID면 GUEST_ID를 출력
    // 그리고 내가 GUEST_ID면 1, HOST_ID면 0
    // 그리고 chat에서 CRATED_AT이 가장 최신인 메시지를 가지고옴(chat_room_id)
    // 그리고 해당 메시지의 결제여부 및 TANSMITTER_ID 출력
    // 쿼리를 2번날려야함
}

/*
    특정 채팅방 조회 -> 특정 채팅방의 내용들을 모두 다 가지고 온다.
    -> 특정 채팅방을 클릭하면 우선, 헤더로 날라온 값에 해당하는 사람이
    HOST(질문자)인지 GUEST(답변자)인지 체크 답변자의 경우 모든 메시지를 전체 공개한다
    그리고 해당 채팅방의 IS_READ(TRANSMITTER_ID가 본인이 아닌)를 true로 설정한다.
    -> 질문자의 경우 TRANSMITTER_ID가 본인이 아닌데 IS_PAID가 false인 경우 메시지를 숨긴다.
    (단! 구독권 모듈에 구독권 정보를 조회 후 존재하면 메시지를 숨기지 않는다 -> 서킷 브레이커)
    그리고 해당 채팅방의 IS_READ(TRANSMITTER_ID가 본인이 아닌)를 true로 설정한다
    -> /chatting/rooms/{roomId}

    서킷브레이커 하면 뭔가 값은 떨궈주는데 코드를 다르게 해가지고 뭔가 메시지를 떨궈야 할 것 같은데
    예를들면 내가 구독권이 있는지 없는지 모르지만 구독권 모듈을 호출을 못했으면
    "구독권"관련 서버에서 문제가 발생하여 구독권 정보가 존재해도 확인할 수 없습니다? 이런식?

    채팅방 만들기
    -> 채팅방을 만들 수 있다.
 */