package com.bitbox.chatting.feign;

import com.bitbox.chatting.dto.SubscriptionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="payment-service")
public interface PaymentFeignServiceClient {
    @GetMapping("/member/subscription")
    SubscriptionResponseDto getSubscription();

    @GetMapping("/member/{memberId}/subscription")
    SubscriptionResponseDto getSubscription(@PathVariable String memberId);
}