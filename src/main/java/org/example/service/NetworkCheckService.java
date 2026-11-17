package org.example.service;

import org.example.service.model.CheckResult;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class NetworkCheckService {

    public static final int DEFAULT_TIMEOUT_MS = 3000;
    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]{1,253}$");

    public boolean isValidHost(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String trimmed = host.trim();
        if (trimmed.startsWith(".") || trimmed.endsWith(".") || trimmed.contains("..")) {
            return false;
        }
        return HOST_PATTERN.matcher(trimmed).matches();
    }

    public boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }

    public CheckResult checkTcpPort(String host, int port) {
        return checkTcpPort(host, port, DEFAULT_TIMEOUT_MS);
    }

    public CheckResult checkTcpPort(String host, int port, int timeoutMs) {
        if (!isValidHost(host)) {
            throw new IllegalArgumentException("Некорректный хост: " + host);
        }
        if (!isValidPort(port)) {
            throw new IllegalArgumentException("Некорректный порт: " + port);
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Таймаут должен быть положительным");
        }

        long startNanos = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host.trim(), port), timeoutMs);
            long rttMillis = (System.nanoTime() - startNanos) / 1_000_000L;
            return new CheckResult(true, rttMillis, null);
        } catch (SocketTimeoutException e) {
            return new CheckResult(false, -1L, "Превышено время ожидания (" + timeoutMs + " мс)");
        } catch (UnknownHostException e) {
            return new CheckResult(false, -1L, "Неизвестный хост: " + host);
        } catch (IOException e) {
            String reason = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "Соединение отклонено";
            return new CheckResult(false, -1L, reason);
        }
    }
}