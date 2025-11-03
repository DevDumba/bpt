package com.example.bpt.controller;

import com.example.bpt.dto.TransferRequest;
import com.example.bpt.dto.TransferResponse;
import com.example.bpt.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfer API", description = "Operations related to payment transfers between accounts")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @Operation(
            summary = "Execute a fund transfer",
            description = "Transfers funds between two accounts within the same banking platform.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer successful",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TransferResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<TransferResponse> transferFunds(@RequestBody TransferRequest request) {
        // Delegate transfer logic to service layer
        TransferResponse response = transferService.transferFunds(request);

        // Return a successful response containing transfer details
        return ResponseEntity.ok(response);
    }
}
