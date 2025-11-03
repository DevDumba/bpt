package com.example.bpt.service.impl;

import com.example.bpt.dto.TransferRequest;
import com.example.bpt.dto.TransferResponse;
import com.example.bpt.event.TransferCompletedEvent;
import com.example.bpt.kafka.TransferEventProducer;
import com.example.bpt.model.Account;
import com.example.bpt.model.Transfer;
import com.example.bpt.repository.AccountRepository;
import com.example.bpt.repository.TransferRepository;
import com.example.bpt.service.TransferService;
import com.example.bpt.util.AccountUtils;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import com.example.bpt.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class TransferServiceImpl implements TransferService {

    @Autowired
    private  AccountRepository accountRepository;
    @Autowired
    private  TransferRepository transferRepository;
    @Autowired
    private TransferEventProducer transferEventProducer;

    @Override
    @Transactional
    public TransferResponse transferFunds(TransferRequest request) {

        log.info("Initiating transfer from {} to {} amount {}",
                request.sourceAccount(), request.destinationAccount(), request.amount());

        // Step 1: Validate request format and business rules (e.g., positive amount, distinct accounts)
        validateRequest(request);

        // Step 2: Retrieve and verify source and destination accounts
        Account source = findAccount(request.sourceAccount(), "source");
        Account destination = findAccount(request.destinationAccount(), "destination");

        // Step 3: Ensure the source account has sufficient balance for this transaction
        validateFunds(source, request.amount());

        // Step 4: Apply balance changes atomically in memory (database persistence occurs in this transaction)
        updateBalances(source, destination, request.amount());

        // Step 5: Persist the transfer record and emit Kafka event for audit/tracking
        Transfer transfer = recordTransfer(source, destination, request.amount());
        log.info("Transfer completed successfully. ID: {}", transfer.getId());

        // Step 6: Return response object containing transfer summary
        return mapToResponse(transfer);

    }

    private TransferResponse mapToResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getAccountNumber(),
                transfer.getDestinationAccount().getAccountNumber(),
                transfer.getAmount(),
                "SUCCESS",
                transfer.getTimestamp()
        );
    }

    private Transfer recordTransfer(Account source, Account destination, @NotNull @DecimalMin(value = "0.01", message = "Transfer amount must be positive") BigDecimal amount) {
        // Build a Transfer entity capturing before/after balance states for both accounts
        Transfer transfer = Transfer.builder()
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(amount)
                .sourceOldBalance(source.getBalance().add(amount))
                .sourceNewBalance(source.getBalance())
                .destinationOldBalance(destination.getBalance().subtract(amount))
                .destinationNewBalance(destination.getBalance())
                .timestamp(LocalDateTime.now())
                .performedBy(source.getOwner()) // Audit: which user initiated the transfer
                .build();

        // Persist transfer record for audit and history tracking
        transfer = transferRepository.save(transfer);

        // Attempt to publish Kafka event (non-blocking; failure is logged but ignored)
        try {
            transferEventProducer.sendTransferCompletedEvent(
                    new TransferCompletedEvent(
                            source.getAccountNumber(),
                            destination.getAccountNumber(),
                            amount,
                            LocalDateTime.now()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send Kafka event for transfer ID {}: {}", transfer.getId(), e.getMessage());
        }

        return transfer;
    }

    private void updateBalances(Account source, Account destination, @NotNull @DecimalMin(value = "0.01", message = "Transfer amount must be positive") BigDecimal amount) {
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        accountRepository.save(source);
        accountRepository.save(destination);
    }

    private void validateFunds(Account source, @NotNull @DecimalMin(value = "0.01", message = "Transfer amount must be positive") BigDecimal amount) {
        if (source.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds on source account");
        }
    }

    public Account findAccount(String accountNumber, String type) {
        String normalized = AccountUtils.normalizeAccountNumber(accountNumber);
        return accountRepository.findByAccountNumber(normalized)
                .orElseThrow(() -> new ResourceNotFoundException(type + " account not found: " + normalized));
    }

    private void validateRequest(TransferRequest request) {
        if (request.sourceAccount() == null || request.destinationAccount() == null) {
            throw new IllegalArgumentException("Source and destination account are required");
        }
        if (request.sourceAccount().equals(request.destinationAccount())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
    }
}
