package com.bitbox.chatting.feign;

import io.github.bitbox.bitbox.dto.MemberCreditDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="user-service")
public interface UserFeignServiceClient {
    @PutMapping ("/member/credit")
    ResponseEntity<Void> updateMemberCredit(@RequestBody MemberCreditDto memberCreditDto);
}
