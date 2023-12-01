package com.slcbudget.eventmanager.domain.dto;

import java.math.BigDecimal;

public record PayRequestDTO(Long creditorId, Long debtorId, BigDecimal amount) {
  
}
