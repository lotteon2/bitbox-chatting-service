package com.bitbox.chatting.feign;

import com.bitbox.chatting.dto.SubscriptionResponseDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "feignClient",
            url = "localhost:8000")
public interface FeignServiceClient {
    @GetMapping("/payment-service/member/subscription")
    @Headers("Authorization")
    SubscriptionResponseDto getSubscription();

    @GetMapping("/payment-service/member/{memberId}/subscription")
    @Headers("Authorization")
    SubscriptionResponseDto getSubscription(@PathVariable String memberId);

    @PatchMapping("/user-service/member/credit")
    @Headers("Authorization")
    ResponseEntity<Void> updateMemberCredit(int credit);

}