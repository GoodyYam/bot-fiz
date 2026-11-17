package org.example.service;

import org.example.service.model.CheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

class NetworkCheckServiceTest {

    private NetworkCheckService service;

    @BeforeEach
    void setUp() {
        service = new NetworkCheckService();
    }

    @Test
    void testHostValidation() {
        assertTrue(service.isValidHost("127.0.0.1"));
        assertTrue(service.isValidHost("google.com"));
        assertTrue(service.isValidHost("sub.domain-1.org"));

        assertFalse(service.isValidHost(null));
        assertFalse(service.isValidHost(""));
        assertFalse(service.isValidHost("   "));
        assertFalse(service.isValidHost("bad host"));
        assertFalse(service.isValidHost("google.com;rm"));
        assertFalse(service.isValidHost(".example.com"));
        assertFalse(service.isValidHost("example..com"));
    }

    @Test
    void testPortValidation() {
        assertTrue(service.isValidPort(1));
        assertTrue(service.isValidPort(80));
        assertTrue(service.isValidPort(443));
        assertTrue(service.isValidPort(65535));

        assertFalse(service.isValidPort(0));
        assertFalse(service.isValidPort(-1));
        assertFalse(service.isValidPort(65536));
    }

    @Test
    void testCheckTcpPort_OpenLocalPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int localPort = serverSocket.getLocalPort();

            CheckResult result = service.checkTcpPort("127.0.0.1", localPort, 1000);

            assertTrue(result.reachable());
            assertTrue(result.rttMillis() >= 0L);
            assertNull(result.errorMessage());
        }
    }

    @Test
    void testCheckTcpPort_ClosedLocalPort() throws IOException {
        int closedPort;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            closedPort = serverSocket.getLocalPort();
        }

        CheckResult result = service.checkTcpPort("127.0.0.1", closedPort, 500);

        assertFalse(result.reachable());
        assertEquals(-1L, result.rttMillis());
        assertNotNull(result.errorMessage());
    }

    @Test
    void testCheckTcpPort_InvalidArgumentsThrowException() {
        assertThrows(IllegalArgumentException.class, () -> service.checkTcpPort("bad host", 80));
        assertThrows(IllegalArgumentException.class, () -> service.checkTcpPort("127.0.0.1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.checkTcpPort("127.0.0.1", 70000));
        assertThrows(IllegalArgumentException.class, () -> service.checkTcpPort("127.0.0.1", 80, 0));
    }
}