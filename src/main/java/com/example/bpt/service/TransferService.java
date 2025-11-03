package com.example.bpt.service;

import com.example.bpt.dto.TransferRequest;
import com.example.bpt.dto.TransferResponse;

public interface TransferService {

    /**
     * Processes a fund transfer between two accounts.
     *
     * @param request Transfer details (source, destination, amount)
     * @return Response with transfer status and info
     */
    TransferResponse transferFunds(TransferRequest request);
}
