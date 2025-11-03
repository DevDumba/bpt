package com.example.bpt;

import com.example.bpt.dto.TransferRequest;
import com.example.bpt.dto.TransferResponse;
import com.example.bpt.model.Account;
import com.example.bpt.model.Transfer;
import com.example.bpt.repository.AccountRepository;
import com.example.bpt.repository.TransferRepository;
import com.example.bpt.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransferServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Account source;
    private Account destination;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        source = Account.builder()
                .accountNumber("205-0000001234567-68")
                .balance(BigDecimal.valueOf(1000))
                .build();

        destination = Account.builder()
                .accountNumber("205-0000007654321-68")
                .balance(BigDecimal.valueOf(500))
                .build();
    }

    @Test
    void testTransferFunds_Success() {
// given
        TransferRequest request = new TransferRequest("205-1234567-68", "205-7654321-68", BigDecimal.valueOf(200));

        Account source = Account.builder()
                .accountNumber("205-0000001234567-68")
                .balance(BigDecimal.valueOf(1000))
                .build();

        Account destination = Account.builder()
                .accountNumber("205-0000007654321-68")
                .balance(BigDecimal.valueOf(500))
                .build();

        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.of(source))
                .thenReturn(Optional.of(destination));

        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> {
                    Transfer t = invocation.getArgument(0);
                    t.setId(1L);
                    return t;
                });

        // when
        TransferResponse result = transferService.transferFunds(request);

        // then
        assertEquals(BigDecimal.valueOf(800), source.getBalance());
        assertEquals(BigDecimal.valueOf(700), destination.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transferRepository, times(1)).save(any(Transfer.class));
        assertNotNull(result);
        assertEquals("205-0000001234567-68", result.sourceAccount());
        assertEquals("205-0000007654321-68", result.destinationAccount());
        assertEquals(BigDecimal.valueOf(200), result.amount());
        assertEquals("SUCCESS", result.status());
    }

    @Test
    void testTransferFunds_InsufficientFunds() {
        TransferRequest request = new TransferRequest("205-1234567-68", "205-7654321-68", BigDecimal.valueOf(20000));

        Account source = Account.builder()
                .accountNumber("205-0000001234567-68")
                .balance(BigDecimal.valueOf(1000))
                .build();

        Account destination = Account.builder()
                .accountNumber("205-0000007654321-68")
                .balance(BigDecimal.valueOf(500))
                .build();

        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.of(source))
                .thenReturn(Optional.of(destination));

        // when + then
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> transferService.transferFunds(request));

        assertEquals("Insufficient funds on source account", ex.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

}
