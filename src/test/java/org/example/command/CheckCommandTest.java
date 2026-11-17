package org.example.command;

import org.example.service.NetworkCheckService;
import org.example.service.model.CheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckCommandTest {

    private NetworkCheckService stubService;
    private CheckCommand command;

    @BeforeEach
    void setUp() {
        stubService = new NetworkCheckService() {
            @Override
            public CheckResult checkTcpPort(String host, int port) {
                if ("127.0.0.1".equals(host) && port == 5432) {
                    return new CheckResult(true, 12L, null);
                }
                return new CheckResult(false, -1L, "Превышено время ожидания (3000 мс)");
            }
        };
        command = new CheckCommand(stubService);
    }

    @Test
    void testMetadata() {
        assertEquals("check", command.getName());
        assertFalse(command.getDescription().isEmpty());
    }

    @Test
    void testWrongArgumentCount() {
        assertTrue(command.execute(new String[0]).contains("Использование: /check <host> <port>"));
        assertTrue(command.execute(new String[]{"127.0.0.1"}).contains("Использование: /check <host> <port>"));
        assertTrue(command.execute(new String[]{"127.0.0.1", "80", "extra"}).contains("Использование: /check <host> <port>"));
    }

    @Test
    void testInvalidHostAndPortValidation() {
        assertTrue(command.execute(new String[]{"invalid_host!", "80"}).contains("некорректное имя хоста"));
        assertTrue(command.execute(new String[]{"127.0.0.1", "abc"}).contains("целым числом"));
        assertTrue(command.execute(new String[]{"127.0.0.1", "0"}).contains("диапазоне от 1 до 65535"));
        assertTrue(command.execute(new String[]{"127.0.0.1", "65536"}).contains("диапазоне от 1 до 65535"));
    }

    @Test
    void testReachableHostOutput() {
        String output = command.execute(new String[]{"127.0.0.1", "5432"});
        assertTrue(output.contains("127.0.0.1:5432"));
        assertTrue(output.contains("Доступен"));
        assertTrue(output.contains("12 мс"));
    }

    @Test
    void testUnreachableHostOutput() {
        String output = command.execute(new String[]{"example.com", "8080"});
        assertTrue(output.contains("example.com:8080"));
        assertTrue(output.contains("Недоступен"));
        assertTrue(output.contains("Превышено время ожидания"));
    }
}