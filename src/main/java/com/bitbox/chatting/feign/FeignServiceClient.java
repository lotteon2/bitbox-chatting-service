package com.bitbox.chatting.feign;

import com.bitbox.chatting.dto.SubscriptionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

@FeignClient(name = "apigateway-service")
public interface FeignServiceClient {
    @GetMapping("/payment-service/subscription")
    SubscriptionResponse getSubscription();

    @PatchMapping("/user-service/member/credit")
    ResponseEntity<Void> updateMemberCredit();
}