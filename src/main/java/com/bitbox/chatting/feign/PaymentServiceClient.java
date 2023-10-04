package com.bitbox.chatting.feign;

import com.bitbox.chatting.dto.SubscriptionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "apigateway-service")
public interface PaymentServiceClient {
    @GetMapping("/payment-service/subscription")
    SubscriptionResponse getSubscription();
}
