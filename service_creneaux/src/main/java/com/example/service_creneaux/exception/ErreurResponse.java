package com.example.service_creneaux.exception;

import java.time.Instant;

public record ErreurResponse(
        Instant timestamp,
        int status,
        String error,
        String message) {
}