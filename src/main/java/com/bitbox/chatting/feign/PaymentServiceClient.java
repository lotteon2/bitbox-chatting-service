package com.bitbox.chatting.feign;

import com.bitbox.chatting.dto.SubscriptionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    @GetMapping("/subscription")
    SubscriptionResponse getSubscription();
}
