package org.example.service.model;

public record ServiceStatusResult(
        boolean active,
        String status,
        String errorMessage
) {
}