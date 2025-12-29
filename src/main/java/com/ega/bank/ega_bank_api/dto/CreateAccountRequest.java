package com.ega.bank.ega_bank_api.dto;

import com.ega.bank.ega_bank_api.model.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {
    @NotNull(message = "clientId is required")
    private Long clientId;

    @NotNull(message = "type is required")
    private AccountType type;
}
