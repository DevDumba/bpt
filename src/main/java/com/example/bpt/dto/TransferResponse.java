package com.example.bpt.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long transferId,
        String sourceAccount,
        String destinationAccount,
        BigDecimal amount,
        String status,
        LocalDateTime timestamp
) {}
