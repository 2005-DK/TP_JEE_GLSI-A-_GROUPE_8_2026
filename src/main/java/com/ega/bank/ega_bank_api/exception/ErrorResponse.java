package com.ega.bank.ega_bank_api.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private final String error;
    private final String message;
    private final int status;
    private final Instant timestamp;
    private final Map<String, String> fields;

    public ErrorResponse(String error, String message, int status, Map<String, String> fields) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = Instant.now();
        this.fields = fields;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}
