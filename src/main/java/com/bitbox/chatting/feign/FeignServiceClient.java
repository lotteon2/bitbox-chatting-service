package com.bitbox.chatting.feign;

import com.bitbox.chatting.dto.SubscriptionResponseDto;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "apigateway-service")
public interface FeignServiceClient {
    // [TODO] 헤더를 달아서 요청을 보낸다
    @GetMapping("/payment-service/member/subscription")
    SubscriptionResponseDto getSubscription();

    @GetMapping("/payment-service/member/{memberId}/subscription")
    SubscriptionResponseDto getSubscription(@PathVariable String memberId);

    @PatchMapping("/user-service/member/credit")
    ResponseEntity<Void> updateMemberCredit(int credit);

    // TODO 무슨 토큰이더라?
    /*@Bean
    default RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer YourAccessToken");
            template.header("CustomHeader", "CustomValue");
        };
    }*/
}