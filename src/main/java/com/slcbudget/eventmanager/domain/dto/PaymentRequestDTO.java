package com.slcbudget.eventmanager.domain.dto;

import java.math.BigDecimal;

public record PaymentRequestDTO(Long activityId, Long payerId, Long eventId, BigDecimal amount) {
}
