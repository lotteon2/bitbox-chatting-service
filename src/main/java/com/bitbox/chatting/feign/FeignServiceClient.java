package com.bitbox.chatting.feign;

import com.bitbox.chatting.config.FeignServiceClientConfig;
import com.bitbox.chatting.dto.SubscriptionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "apigateway-service", configuration = FeignServiceClientConfig.class)
public interface FeignServiceClient {
    @GetMapping("/payment-service/member/subscription")
    SubscriptionResponseDto getSubscription();

    @GetMapping("/payment-service/member/{memberId}/subscription")
    SubscriptionResponseDto getSubscription(@PathVariable String memberId);

    @PatchMapping("/user-service/member/credit")
    ResponseEntity<Void> updateMemberCredit(int credit);

}