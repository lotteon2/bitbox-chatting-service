package com.bitbox.chatting.dto;

import io.github.bitbox.bitbox.enums.SubscriptionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionResponseDto {
    private boolean isValid;
    private SubscriptionType subscriptionType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
