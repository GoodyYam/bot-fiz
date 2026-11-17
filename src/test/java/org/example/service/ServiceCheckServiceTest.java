package org.example.service;

import org.example.service.model.ServiceStatusResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCheckServiceTest {

    private ServiceCheckService service;

    @BeforeEach
    void setUp() {
        service = new ServiceCheckService();
    }
    @Test
    void testServiceNameRegexValidation() {
        assertTrue(service.isValidServiceName("nginx"));
        assertTrue(service.isValidServiceName("postgresql"));
        assertTrue(service.isValidServiceName("docker"));
        assertTrue(service.isValidServiceName("ssh"));
        assertTrue(service.isValidServiceName("my_service-1"));
        assertFalse(service.isValidServiceName(null));
        assertFalse(service.isValidServiceName(""));
        assertFalse(service.isValidServiceName("   "));
        assertFalse(service.isValidServiceName("nginx; rm -rf /"));
        assertFalse(service.isValidServiceName("nginx;rm"));
        assertFalse(service.isValidServiceName("nginx && whoami"));
        assertFalse(service.isValidServiceName("nginx | cat /etc/passwd"));
        assertFalse(service.isValidServiceName("$(reboot)"));
        assertFalse(service.isValidServiceName("`id`"));
        assertFalse(service.isValidServiceName("../etc/init.d"));
    }

    @Test
    void testWhitelistValidation() {
        // Службы из белого списка
        assertTrue(service.isAllowedService("nginx"));
        assertTrue(service.isAllowedService("NGINX"));
        assertTrue(service.isAllowedService("postgresql"));
        assertTrue(service.isAllowedService("docker"));
        assertTrue(service.isAllowedService("ssh"));
        assertFalse(service.isAllowedService("apache2"));
        assertFalse(service.isAllowedService("mysql"));
        assertFalse(service.isAllowedService("redis"));
        assertFalse(service.isAllowedService("nginx; rm -rf /"));
    }

    @Test
    void testCheckServiceStatus_InvalidArgumentsThrowException() {
        assertThrows(IllegalArgumentException.class, () -> service.checkServiceStatus("nginx; rm -rf /"));
        assertThrows(IllegalArgumentException.class, () -> service.checkServiceStatus("apache2"));
        assertThrows(IllegalArgumentException.class, () -> service.checkServiceStatus("nginx", 0));
    }

    @Test
    void testCheckServiceStatus_ReturnsResultWithoutCrashing() {
        ServiceStatusResult result = service.checkServiceStatus("nginx", 1000);
        assertNotNull(result);
        assertNotNull(result.status());
    }
}