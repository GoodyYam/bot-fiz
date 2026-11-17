package org.example.service;

import org.example.service.model.ServiceStatusResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ServiceCheckService {

    private static final Pattern SAFE_SERVICE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private static final Set<String> DEFAULT_ALLOWED_SERVICES = Set.of(
            "nginx",
            "postgresql",
            "docker",
            "ssh"
    );

    private static final int DEFAULT_TIMEOUT_MILLIS = 3000;

    private final Set<String> allowedServices;

    public ServiceCheckService() {
        this(DEFAULT_ALLOWED_SERVICES);
    }

    public ServiceCheckService(Set<String> allowedServices) {
        this.allowedServices = Collections.unmodifiableSet(allowedServices);
    }

    public boolean isValidServiceName(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return false;
        }
        return SAFE_SERVICE_NAME_PATTERN.matcher(serviceName).matches();
    }

    public boolean isAllowedService(String serviceName) {
        if (!isValidServiceName(serviceName)) {
            return false;
        }
        return allowedServices.contains(serviceName.toLowerCase());
    }

    public Set<String> getAllowedServices() {
        return allowedServices;
    }

    public ServiceStatusResult checkServiceStatus(String serviceName) {
        return checkServiceStatus(serviceName, DEFAULT_TIMEOUT_MILLIS);
    }

    public ServiceStatusResult checkServiceStatus(String serviceName, int timeoutMillis) {
        if (!isValidServiceName(serviceName)) {
            throw new IllegalArgumentException("Недопустимое имя службы: " + serviceName);
        }
        if (!isAllowedService(serviceName)) {
            throw new IllegalArgumentException("Служба не входит в белый список: " + serviceName);
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("Таймаут должен быть положительным числом.");
        }

        String normalizedName = serviceName.toLowerCase();
        ProcessBuilder processBuilder = new ProcessBuilder("systemctl", "is-active", normalizedName);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);

            if (!finished) {
                process.destroyForcibly();
                return new ServiceStatusResult(
                        false,
                        "timeout",
                        "Превышено время ожидания ответа от systemctl (" + timeoutMillis + " мс)"
                );
            }

            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                output = (line != null && !line.isBlank()) ? line.trim() : "unknown";
            }

            int exitCode = process.exitValue();
            if (exitCode == 0 && "active".equalsIgnoreCase(output)) {
                return new ServiceStatusResult(true, "active", null);
            }

            return new ServiceStatusResult(
                    false,
                    output,
                    "Служба находится в состоянии '" + output + "' (код возврата: " + exitCode + ")"
            );

        } catch (IOException e) {
            return new ServiceStatusResult(
                    false,
                    "unavailable",
                    "Системная утилита systemctl недоступна в данном окружении"
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ServiceStatusResult(
                    false,
                    "interrupted",
                    "Проверка состояния службы была прервана"
            );
        }
    }
}