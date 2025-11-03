package com.example.bpt.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCompletedEvent(
        String sourceAccount,
        String destinationAccount,
        BigDecimal amount,
        LocalDateTime timestamp
) {}