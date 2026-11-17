package org.example.service.model;

public record CheckResult(
        boolean reachable,
        long rttMillis,
        String errorMessage
) {
}